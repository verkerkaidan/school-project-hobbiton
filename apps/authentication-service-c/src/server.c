/* server.c - production-safe threaded HTTP server with auth_core routing */

#define _GNU_SOURCE
#include <arpa/inet.h>
#include <errno.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <pthread.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <unistd.h>
#include <ctype.h>
#include <stdarg.h>
#include <time.h>

#include "auth_core.h"

#define PORT 21525
#define BACKLOG 128

#define WORKER_THREADS 8
#define MAX_QUEUE_SIZE 64
#define BUF_SIZE 16384

typedef struct {
    int fds[MAX_QUEUE_SIZE];
    int front, rear, size;
    pthread_mutex_t mutex;
    pthread_cond_t not_empty;
    pthread_cond_t not_full;
} conn_queue_t;

typedef enum {
    LOG_INFO,
    LOG_WARN,
    LOG_ERROR
} log_level_t;

static pthread_mutex_t log_mutex = PTHREAD_MUTEX_INITIALIZER;
static FILE *log_file = NULL;
static conn_queue_t queue;

/* ---------------- Queue ---------------- */

static void queue_init(conn_queue_t *q) {
    memset(q, 0, sizeof(*q));
    pthread_mutex_init(&q->mutex, NULL);
    pthread_cond_init(&q->not_empty, NULL);
    pthread_cond_init(&q->not_full, NULL);
}

static void queue_push(conn_queue_t *q, int fd) {
    pthread_mutex_lock(&q->mutex);
    while (q->size == MAX_QUEUE_SIZE)
        pthread_cond_wait(&q->not_full, &q->mutex);

    q->fds[q->rear] = fd;
    q->rear = (q->rear + 1) % MAX_QUEUE_SIZE;
    q->size++;

    pthread_cond_signal(&q->not_empty);
    pthread_mutex_unlock(&q->mutex);
}

static int queue_pop(conn_queue_t *q) {
    pthread_mutex_lock(&q->mutex);
    while (q->size == 0)
        pthread_cond_wait(&q->not_empty, &q->mutex);

    int fd = q->fds[q->front];
    q->front = (q->front + 1) % MAX_QUEUE_SIZE;
    q->size--;

    pthread_cond_signal(&q->not_full);
    pthread_mutex_unlock(&q->mutex);
    return fd;
}

/* ---------------- Logging ---------------- */

static const char *level_str(log_level_t lvl) {
    return (lvl == LOG_INFO) ? "INFO" :
           (lvl == LOG_WARN) ? "WARN" : "ERROR";
}

static void log_msg(log_level_t level, const char *fmt, ...) {
    pthread_mutex_lock(&log_mutex);

    time_t now = time(NULL);
    struct tm t;
    localtime_r(&now, &t);
    char timebuf[32];

    strftime(timebuf, sizeof(timebuf),
             "%Y-%m-%d %H:%M:%S",
             &t);



    fprintf(log_file, "[%s] [%s] ", timebuf, level_str(level));

    va_list args;
    va_start(args, fmt);
    vfprintf(log_file, fmt, args);
    va_end(args);

    fprintf(log_file, "\n");
    fflush(log_file);

    pthread_mutex_unlock(&log_mutex);
}

/* ---------------- HTTP Helpers ---------------- */

static void send_response(int client, int status, const char *status_text,
                          const char *body, const char *ctype) {
    char header[1024];
    int len = body ? (int)strlen(body) : 0;

    snprintf(header, sizeof(header),
        "HTTP/1.1 %d %s\r\n"
        "Content-Type: %s\r\n"
        "Content-Length: %d\r\n"
        "Connection: close\r\n"
        "Access-Control-Allow-Origin: *\r\n"
        "Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS\r\n"
        "Access-Control-Allow-Headers: Content-Type, Authorization\r\n"
        "\r\n",
        status, status_text,
        ctype ? ctype : "application/json",
        len);

    send(client, header, strlen(header), 0);
    if (len > 0) send(client, body, len, 0);
}

static char *json_get_string_local(const char *json, const char *key) {
    if(!json || !key) return NULL;

    char pattern[128];
    snprintf(pattern, sizeof(pattern), "\"%s\"", key);

    char *p = strstr(json, pattern);
    if (!p) return NULL;

    p = strchr(p, ':');
    if (!p) return NULL;
    p++;

    while (*p && isspace((unsigned char)*p)) p++;
    if (*p != '"') return NULL;
    p++;

    const char *start = p;
    while (*p && *p != '"') {
        if (*p == '\\' && *(p + 1)) p += 2;
        else p++;
    }

    size_t len = p - start;
    char *out = malloc(len + 1);
    strncpy(out, start, len);
    out[len] = '\0';
    return out;
}


static char *extract_bearer_token(const char *headers) {
    const char *p = strcasestr(headers, "Authorization:");
    if (!p) return NULL;

    const char *colon = strchr(p, ':');
    if (!colon) return NULL;
    colon++;

    while (*colon && isspace((unsigned char)*colon)) colon++;

    if (strncasecmp(colon, "Bearer ", 7) != 0) return NULL;

    const char *tok = colon + 7;
    const char *end = strstr(tok, "\r\n");
    if (!end) end = tok + strlen(tok);

    return strndup(tok, end - tok);
}

static void handle_client_fd(int client) {
    char buffer[BUF_SIZE + 1];
    size_t total_received = 0;

    /* Read until headers are complete */
    char *headers_end = NULL;

    while (!headers_end) {
        ssize_t n = recv(
            client,
            buffer + total_received,
            BUF_SIZE - total_received,
            0
        );

        if (n <= 0) {
            close(client);
            return;
        }

        total_received += n;
        buffer[total_received] = '\0';

        headers_end = strstr(buffer, "\r\n\r\n");

        if (total_received >= BUF_SIZE) {
            send_response(
                client,
                413,
                "Payload Too Large",
                "{\"error\":\"Request too large\"}",
                "application/json"
            );
            close(client);
            return;
        }
    }

    size_t headers_len = (headers_end - buffer) + 4;

    char method[8];
    char path[512];

    if (sscanf(buffer, "%7s %511s", method, path) < 2) {
        send_response(
            client,
            400,
            "Bad Request",
            "{\"error\":\"Bad request line\"}",
            "application/json"
        );
        close(client);
        return;
    }

    char *headers = strndup(buffer, headers_len);

    /* Parse Content-Length */
    int content_length = 0;

    char *cl = strstr(headers, "Content-Length:");
    if (cl) {
        sscanf(cl, "Content-Length: %d", &content_length);
    }

    char *body = NULL;

    if (content_length > 0) {

        body = malloc(content_length + 1);

        if (!body) {
            free(headers);
            close(client);
            return;
        }

        size_t already_have = total_received - headers_len;

        if (already_have > (size_t)content_length) {
            already_have = content_length;
        }

        memcpy(
            body,
            buffer + headers_len,
            already_have
        );

        size_t body_received = already_have;

        while (body_received < (size_t)content_length) {

            ssize_t n = recv(
                client,
                body + body_received,
                content_length - body_received,
                0
            );

            if (n <= 0) {
                free(body);
                free(headers);
                close(client);
                return;
            }

            body_received += n;
        }

        body[content_length] = '\0';
    }

    log_msg(LOG_INFO, "%s %s", method, path);

    if (body) {
        log_msg(LOG_INFO, "BODY: %s", body);
    }


    if (strcmp(method, "POST") == 0 && strcmp(path, "/api/auth/register") == 0) {
        log_msg(LOG_INFO, "REGISTER START");

        if (!body) {
            send_response(client, 400, "Bad Request",
                          "{\"error\":\"missing body\"}",
                          "application/json");
            goto cleanup;
        }

        char *email = json_get_string_local(body, "email");
        log_msg(LOG_INFO, "email extracted");

        char *password = json_get_string_local(body, "password");
        log_msg(LOG_INFO, "password extracted");

        if (!email || !password) {
            send_response(client, 400, "Bad Request",
                                      "{\"error\":\"missing fields\"}", "application/json");
        } else {

            log_msg(LOG_INFO, "calling add_user_db");

            char new_id[37];

            int rc = add_user_db(
                email,
                password,
                new_id,
                sizeof(new_id)
            );

            log_msg(LOG_INFO, "add_user_db returned %d", rc);

            if (rc != 0) {
                send_response(client, 500, "Error",
                    "{\"error\":\"create failed\"}", "application/json");
            } else {

                log_msg(LOG_INFO, "calling jwt_generate");

                char roles[64] = "ROLE_USER";

                char *token = jwt_generate(new_id, roles);

                log_msg(LOG_INFO, "jwt_generate returned");

                char out[512];
                snprintf(out, sizeof(out),
                         "{\"userId\":\"%s\",\"token\":\"%s\"}",
                         new_id, token ? token : "");

                send_response(client, 200, "OK", out, "application/json");
                free(token);
                }
            }
        free(email);
        free(password);
    }
    else if (strcmp(method, "POST") == 0 && strcmp(path, "/api/auth/login") == 0) {
        char *email = json_get_string_local(body, "email");
        char *password = json_get_string_local(body, "password");

        char id[37], roles[512];

        if (authenticate_user_db(email, password, id, sizeof(id), roles, sizeof(roles)) != 0) {
            send_response(client, 403, "Forbidden",
                          "{\"error\":\"invalid credentials\"}", "application/json");
        } else {
            char *token = jwt_generate(id, roles);

            char out[512];
            snprintf(out, sizeof(out),
                     "{\"userId\":\"%s\",\"token\":\"%s\"}",
                     id, token ? token : "");

            send_response(client, 200, "OK", out, "application/json");
            free(token);
        }

        free(email);
        free(password);
    }
     else if (strcmp(method, "DELETE") == 0 && strcmp(path, "/api/auth/delete") == 0) {
        char *token = extract_bearer_token(headers);
        if (!token) { send_response(client, 401, "Unauthorized", "{\"error\":\"Missing token\"}", "application/json"); }
        else {
            char *userId = NULL; char *roles_csv = NULL;
            if (jwt_validate_and_extract(token, &userId, &roles_csv) != 0) { send_response(client, 401, "Unauthorized", "{\"error\":\"Invalid token\"}", "application/json"); }
            else {
                char *email = json_get_string_local(body, "email");
                char *password = json_get_string_local(body, "password");
                if (!email || !password) { send_response(client, 400, "Bad Request", "{\"error\":\"email and password required\"}", "application/json"); }
                else {
                    char id[37]; char hash[512]; char roles[512]; int enabled;
                    if (find_user_by_email_db(email, id, sizeof(id), hash, sizeof(hash), roles, sizeof(roles), &enabled) != 0) { send_response(client, 403, "Forbidden", "{\"error\":\"Invalid credentials\"}", "application/json"); }
                    else {
                        char auth_id[37]; char roles2[512];
                        if (authenticate_user_db(email, password, auth_id, sizeof(auth_id), roles2, sizeof(roles2)) != 0 || strcmp(auth_id, userId) != 0) {
                            send_response(client, 403, "Forbidden", "{\"error\":\"Invalid credentials\"}", "application/json");
                        } else {
                            if (delete_user_by_id_db(auth_id) != 0) send_response(client, 500, "Internal Server Error", "{\"error\":\"Could not delete user\"}", "application/json");
                            else send_response(client, 200, "OK", "{\"result\":\"Successfully removed user\"}", "application/json");
                        }
                    }
                }
                free(email); free(password);
            }
            free(token); free(userId); free(roles_csv);
        }
    }
    else if (strcmp(method, "PUT") == 0 && strncmp(path, "/api/auth/admin/new/", 16) == 0) {
        char *token = extract_bearer_token(headers);
        if (!token) { send_response(client, 401, "Unauthorized", "{\"error\":\"Missing token\"}", "application/json"); }
        else {
            char *callerId = NULL; char *caller_roles = NULL;
            if (jwt_validate_and_extract(token, &callerId, &caller_roles) != 0) { send_response(client, 401, "Unauthorized", "{\"error\":\"Invalid token\"}", "application/json"); }
            else {
                int isAdmin = (caller_roles && strstr(caller_roles, "ROLE_ADMIN")) ? 1 : 0;
                if (!isAdmin) send_response(client, 403, "Forbidden", "{\"error\":\"Requires admin role\"}", "application/json");
                else {
                    const char *target = path + 16;
                    if (strlen(target) != 36) send_response(client, 400, "Bad Request", "{\"error\":\"Invalid user id\"}", "application/json");
                    else {
                        if (update_user_role_db(target, "ROLE_ADMIN") != 0) send_response(client, 404, "Not Found", "{\"error\":\"User not found\"}", "application/json");
                        else send_response(client, 200, "OK", "{\"result\":\"Successfully made user admin\"}", "application/json");
                    }
                }
            }
            free(token); free(callerId); free(caller_roles);
        }
    }
    else if (strcmp(method, "PUT") == 0 && strncmp(path, "/api/auth/staff/new/", 16) == 0) {
        char *token = extract_bearer_token(headers);
        if (!token) { send_response(client, 401, "Unauthorized", "{\"error\":\"Missing token\"}", "application/json"); }
        else {
            char *callerId = NULL; char *caller_roles = NULL;
            if (jwt_validate_and_extract(token, &callerId, &caller_roles) != 0) { send_response(client, 401, "Unauthorized", "{\"error\":\"Invalid token\"}", "application/json"); }
            else {
                int isAdmin = (caller_roles && strstr(caller_roles, "ROLE_ADMIN")) ? 1 : 0;
                if (!isAdmin) send_response(client, 403, "Forbidden", "{\"error\":\"Requires admin role\"}", "application/json");
                else {
                    const char *target = path + 16;
                    if (strlen(target) != 36) send_response(client, 400, "Bad Request", "{\"error\":\"Invalid user id\"}", "application/json");
                    else {
                        if (update_user_role_db(target, "ROLE_STAFF") != 0) send_response(client, 404, "Not Found", "{\"error\":\"User not found\"}", "application/json");
                        else send_response(client, 200, "OK", "{\"result\":\"Successfully made user staff\"}", "application/json");
                    }
                }
            }
            free(token); free(callerId); free(caller_roles);
        }
    }
    else if (strcmp(method, "GET") == 0 && strcmp(path, "/api/Oauth/dashboard") == 0) {
        char *token = extract_bearer_token(headers);
        if (!token) { send_response(client, 401, "Unauthorized", "{\"error\":\"Missing token\"}", "application/json"); }
        else {
            char *userId = NULL; char *roles = NULL;
            if (jwt_validate_and_extract(token, &userId, &roles) != 0) { send_response(client, 401, "Unauthorized", "{\"error\":\"Invalid token\"}", "application/json"); }
            else {
                // read user from DB
                char id[37]; char hash[512]; char roles_csv[512]; int enabled;
                if (find_user_by_email_db("", id, sizeof(id), hash, sizeof(hash), roles_csv, sizeof(roles_csv), &enabled) == 0) {
                    // we called with empty email, above returns not found; instead query by id is needed but not exposed; keep response generic
                }
                char body_out[512]; snprintf(body_out, sizeof(body_out), "{\"message\":\"Welcome, %s (%s)\"}", userId, userId);
                send_response(client, 200, "OK", body_out, "application/json");
            }
            free(token); free(userId); free(roles);
        }
    }
    else if (strcmp(method, "GET") == 0 && strcmp(path, "/api/Oauth/me") == 0) {
        char *token = extract_bearer_token(headers);

        char *id = NULL, *roles = NULL;

        if (!token || jwt_validate_and_extract(token, &id, &roles) != 0) {
            send_response(client, 401, "Unauthorized",
                          "{\"error\":\"invalid token\"}", "application/json");
        } else {
            char out[512];
            snprintf(out, sizeof(out),
                     "{\"id\":\"%s\",\"roles\":\"%s\"}",
                     id, roles ? roles : "");

            send_response(client, 200, "OK", out, "application/json");
        }

        free(token);
        free(id);
        free(roles);
    }

    else {
        send_response(client, 404, "Not Found",
                      "{\"error\":\"not found\"}", "application/json");
    }

    goto cleanup;
    cleanup:
        free(headers);
        if (body) {
            free(body);
        }
        close(client);

}

static void *worker_loop(void *arg) {
    (void)arg;

    while (1) {
        int client = queue_pop(&queue);
        handle_client_fd(client);
    }
    return NULL;
}

static void *accept_loop(void *arg) {
    int server_fd = *(int *)arg;

    while (1) {
        struct sockaddr_in addr;
        socklen_t len = sizeof(addr);

        int client = accept(server_fd, (struct sockaddr *)&addr, &len);
        if (client < 0) continue;

        queue_push(&queue, client);
    }
    return NULL;
}

/* ---------------- Main ---------------- */

int main() {
    db_init();

    log_file = fopen("server.log", "a");
    if (!log_file) log_file = stdout;

    int server_fd = socket(AF_INET, SOCK_STREAM, 0);

    int opt = 1;
    setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));

    struct sockaddr_in addr = {
        .sin_family = AF_INET,
        .sin_port = htons(PORT),
        .sin_addr.s_addr = INADDR_ANY
    };

    bind(server_fd, (struct sockaddr *)&addr, sizeof(addr));
    listen(server_fd, BACKLOG);

    queue_init(&queue);

    pthread_t workers[WORKER_THREADS];
    for (int i = 0; i < WORKER_THREADS; i++)
        pthread_create(&workers[i], NULL, worker_loop, NULL);

    pthread_t accept_thread;
    pthread_create(&accept_thread, NULL, accept_loop, &server_fd);

    printf("Server running on %d\n", PORT);

    pthread_join(accept_thread, NULL);

    close(server_fd);
    db_close();

    return 0;
}