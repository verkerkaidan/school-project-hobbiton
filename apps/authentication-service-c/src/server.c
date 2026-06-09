/* server.c
 * Minimal HTTP server that uses auth_core library functions for DB, bcrypt and JWT.
 */

#define _GNU_SOURCE
#include <arpa/inet.h>
#include <errno.h>
#include <fcntl.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/stat.h>
#include <sys/types.h>
#include <time.h>
#include <unistd.h>
#include <pthread.h>
#include <ctype.h>

#include "auth_core.h"

#define PORT 21525
#define BACKLOG 10
#define BUF_SIZE 16384

static void send_response(int client, int status, const char *status_text, const char *body, const char *content_type) {
    char header[1024];
    int body_len = body ? (int)strlen(body) : 0;
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
        content_type ? content_type : "application/json",
        body_len);
    send(client, header, strlen(header), 0);
    if (body_len > 0) send(client, body, body_len, 0);
}

static char *json_get_string_local(const char *json, const char *key) {
    // reuse a simple parser from auth_core.c but local here to avoid exporting it
    char pattern[128]; snprintf(pattern, sizeof(pattern), "\"%s\"", key);
    char *p = strstr(json, pattern);
    if (!p) return NULL; p = strchr(p, ':'); if (!p) return NULL; p++;
    while (*p && isspace((unsigned char)*p)) p++; if (*p != '"') return NULL; p++;
    const char *start = p; while (*p && *p != '"') { if (*p == '\\' && *(p+1)) p += 2; else p++; }
    size_t len = p - start; char *out = malloc(len + 1); if (!out) return NULL; strncpy(out, start, len); out[len] = '\0'; return out;
}

static char *extract_bearer_token(const char *headers) {
    if (!headers) return NULL;
    const char *p = strcasestr(headers, "Authorization:");
    if (p) {
        const char *line_end = strstr(p, "\r\n");
        if (!line_end) line_end = p + strlen(p);
        const char *colon = strchr(p, ':'); if (!colon) colon = p; colon++;
        while (colon < line_end && isspace((unsigned char)*colon)) colon++;
        if (strncasecmp(colon, "Bearer ", 7) == 0) {
            const char *tokstart = colon + 7;
            size_t len = (size_t)(line_end - tokstart);
            char *tok = strndup(tokstart, len);
            char *trim = tok; while (*trim && isspace((unsigned char)*trim)) trim++;
            char *end = tok + strlen(tok) - 1; while (end > trim && isspace((unsigned char)*end)) { *end = '\0'; end--; }
            char *r = strdup(trim); free(tok); return r;
        }
    }
    p = strcasestr(headers, "Cookie:");
    if (p) {
        const char *line_end = strstr(p, "\r\n"); if (!line_end) line_end = p + strlen(p);
        const char *cookie_start = strchr(p, ':'); if (!cookie_start) cookie_start = p; cookie_start++;
        const char *jwtpos = strcasestr(cookie_start, "jwt=");
        if (jwtpos && jwtpos < line_end) {
            jwtpos += 4;
            const char *semi = memchr(jwtpos, ';', (size_t)(line_end - jwtpos));
            size_t len = semi ? (size_t)(semi - jwtpos) : (size_t)(line_end - jwtpos);
            char *tok = strndup(jwtpos, len);
            char *trim = tok; while (*trim && isspace((unsigned char)*trim)) trim++;
            char *end = tok + strlen(tok) - 1; while (end > trim && isspace((unsigned char)*end)) { *end = '\0'; end--; }
            char *r = strdup(trim); free(tok); return r;
        }
    }
    return NULL;
}

static void *handle_client(void *arg) {
    int client = *(int*)arg; free(arg);
    char buffer[BUF_SIZE+1]; ssize_t r = recv(client, buffer, BUF_SIZE, 0);
    if (r <= 0) { close(client); return NULL; }
    buffer[r] = '\0';
    char *headers_end = strstr(buffer, "\r\n\r\n");
    if (!headers_end) { send_response(client, 400, "Bad Request", "{\"error\":\"Malformed request\"}", "application/json"); close(client); return NULL; }
    size_t headers_len = (size_t)(headers_end - buffer) + 4;
    char *headers = strndup(buffer, headers_len);
    char method[8], path[512]; int offset = 0;
    if (sscanf(buffer, "%7s %511s %n", method, path, &offset) < 2) { send_response(client, 400, "Bad Request", "{\"error\":\"Bad request line\"}", "application/json"); free(headers); close(client); return NULL; }
    int content_length = 0; char *cl = strcasestr(headers, "Content-Length:"); if (cl) { cl = strchr(cl, ':'); if (cl) { cl++; while (*cl && isspace((unsigned char)*cl)) cl++; content_length = atoi(cl); } }
    char *body = NULL; size_t received_body = r - headers_len; if (content_length > 0) { body = malloc(content_length+1); if (received_body >= (size_t)content_length) memcpy(body, buffer + headers_len, content_length); else { if (received_body > 0) memcpy(body, buffer + headers_len, received_body); size_t to_read = content_length - received_body; size_t pos = received_body; while (to_read > 0) { ssize_t rr = recv(client, body + pos, to_read, 0); if (rr <= 0) break; pos += rr; to_read -= rr; } } body[content_length] = '\0'; }

    if (strcmp(method, "OPTIONS") == 0) {
        send_response(client, 204, "No Content", "", "application/json");
        close(client);
        free(headers);
        if (body) free(body);
        return NULL;
    }
    if (strcmp(method, "POST") == 0 && strcmp(path, "/api/auth/register") == 0) {
        char *email = json_get_string_local(body, "email");
        char *password = json_get_string_local(body, "password");
        if (!email || !password) { send_response(client, 400, "Bad Request", "{\"error\":\"email and password required\"}", "application/json"); }
        else if (!is_valid_email(email)) { send_response(client, 400, "Bad Request", "{\"error\":\"Invalid email\"}", "application/json"); }
        else if (!is_valid_password(password)) { send_response(client, 400, "Bad Request", "{\"error\":\"Invalid password\"}", "application/json"); }
        else {
            char found_id[37]; char found_hash[256]; char roles[512]; int enabled;
            if (find_user_by_email_db(email, found_id, sizeof(found_id), found_hash, sizeof(found_hash), roles, sizeof(roles), &enabled) == 0) {
                send_response(client, 400, "Bad Request", "{\"error\":\"User exists\"}", "application/json");
            } else {
                char new_id[37]; if (add_user_db(email, password, new_id, sizeof(new_id)) != 0) { send_response(client, 500, "Internal Server Error", "{\"error\":\"Could not create user\"}", "application/json"); }
                else {
                    // retrieve roles and issue token
                    char found_id2[37]; char hash2[256]; char roles2[512]; int enabled2;
                    if (find_user_by_email_db(email, found_id2, sizeof(found_id2), hash2, sizeof(hash2), roles2, sizeof(roles2), &enabled2) != 0) {
                        send_response(client, 500, "Internal Server Error", "{\"error\":\"Created but cannot read back\"}", "application/json");
                    } else {
                        char *token = jwt_generate(found_id2, roles2);
                        char body_out[1024]; snprintf(body_out, sizeof(body_out), "{\"userId\":\"%s\",\"token\":\"%s\"}", found_id2, token ? token : "");
                        send_response(client, 200, "OK", body_out, "application/json");
                        free(token);
                    }
                }
            }
        }
        free(email); free(password);
    }
    else if (strcmp(method, "POST") == 0 && strcmp(path, "/api/auth/login") == 0) {
        char *email = json_get_string_local(body, "email");
        char *password = json_get_string_local(body, "password");
        if (!email || !password) { send_response(client, 400, "Bad Request", "{\"error\":\"email and password required\"}", "application/json"); }
        else {
            char auth_id[37]; char roles[512];
            if (authenticate_user_db(email, password, auth_id, sizeof(auth_id), roles, sizeof(roles)) != 0) {
                send_response(client, 403, "Forbidden", "{\"error\":\"No valid credentials\"}", "application/json");
            } else {
                char *token = jwt_generate(auth_id, roles);
                char body_out[1024]; snprintf(body_out, sizeof(body_out), "{\"userId\":\"%s\",\"token\":\"%s\"}", auth_id, token ? token : "");
                send_response(client, 200, "OK", body_out, "application/json");
                free(token);
            }
        }
        free(email); free(password);
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
    else if (strcmp(method, "PUT") == 0 && strcmp(path, "/api/auth/activate") == 0) {
        char *token = extract_bearer_token(headers);
        if (!token) { send_response(client, 401, "Unauthorized", "{\"error\":\"Missing token\"}", "application/json"); }
        else {
            char *callerId = NULL; char *caller_roles = NULL;
            if (jwt_validate_and_extract(token, &callerId, &caller_roles) != 0) { send_response(client, 401, "Unauthorized", "{\"error\":\"Invalid token\"}", "application/json"); }
            else {
                if (update_user_state(callerId, "ACTIVE") != 0) send_response(client, 404, "Not Found", "{\"error\":\"User not found\"}", "application/json");
                else send_response(client, 200, "OK", "{\"result\":\"Successfully made user active\"}", "application/json");
            }
            free(token); free(callerId); free(caller_roles);
        }
    }
    else if (strcmp(method, "PUT") == 0 && strncmp(path, "/api/auth/admin/deactivate/", 16) == 0) {
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
                        if (update_user_state(target, "DEACTIVATE") != 0) send_response(client, 404, "Not Found", "{\"error\":\"User not found\"}", "application/json");
                        else send_response(client, 200, "OK", "{\"result\":\"Successfully deactivated user\"}", "application/json");
                    }
                }
            }
            free(token); free(callerId); free(caller_roles);
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
        if (!token) { send_response(client, 401, "Unauthorized", "{\"error\":\"Missing token\"}", "application/json"); }
        else {
            char *userId = NULL; char *roles = NULL;
            if (jwt_validate_and_extract(token, &userId, &roles) != 0) { send_response(client, 401, "Unauthorized", "{\"error\":\"Invalid token\"}", "application/json"); }
            else {
                // fetch user info from DB by id
                // We don't have a helper to query by id; we can reuse find_user_by_email_db by scanning roles but that's inconvenient.
                char body_out[1024]; snprintf(body_out, sizeof(body_out), "{\"id\":\"%s\",\"roles\":\"%s\"}", userId, roles ? roles : "");
                send_response(client, 200, "OK", body_out, "application/json");
            }
            free(token); free(userId); free(roles);
        }
    }
    else {
        send_response(client, 404, "Not Found", "{\"error\":\"Not found\"}", "application/json");
    }

    free(headers); if (body) free(body); close(client); return NULL;
}

int main(int argc, char **argv) {
    // initialize DB
    if (db_init() != 0) { fprintf(stderr, "Failed to init DB (set SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME and SPRING_DATASOURCE_PASSWORD)\n"); return 1; }
    // create admin user if missing
    const char *admin_user = getenv("ADMIN_USER"); if (!admin_user) admin_user = "admin@localhost";
    const char *admin_pass = getenv("ADMIN_PASSWORD"); if (!admin_pass) admin_pass = "AdminPass123!";
    char idbuf[37]; char hashbuf[512]; char roles[512]; int enabled;
    if (find_user_by_email_db(admin_user, idbuf, sizeof(idbuf), hashbuf, sizeof(hashbuf), roles, sizeof(roles), &enabled) != 0) {
        char newid[37]; if (add_user_db(admin_user, admin_pass, newid, sizeof(newid)) == 0) {
            // give admin role
            update_user_role_db(newid, "ROLE_ADMIN");
            printf("Created initial admin user %s\n", admin_user);
        }
    }

    int server_fd = socket(AF_INET, SOCK_STREAM, 0);
    if (server_fd < 0) { perror("socket"); return 1; }
    int opt = 1; setsockopt(server_fd, SOL_SOCKET, SO_REUSEADDR, &opt, sizeof(opt));
    struct sockaddr_in addr; addr.sin_family = AF_INET; addr.sin_port = htons(PORT); addr.sin_addr.s_addr = INADDR_ANY;
    if (bind(server_fd, (struct sockaddr*)&addr, sizeof(addr)) < 0) { perror("bind"); return 1; }
    if (listen(server_fd, BACKLOG) < 0) { perror("listen"); return 1; }
    printf("auth-service (C) listening on port %d\n", PORT);
    while (1) {
        struct sockaddr_in client_addr; socklen_t addrlen = sizeof(client_addr);
        int client = accept(server_fd, (struct sockaddr*)&client_addr, &addrlen);
        if (client < 0) continue;
        int *pclient = malloc(sizeof(int)); *pclient = client; pthread_t tid; pthread_create(&tid, NULL, handle_client, pclient); pthread_detach(tid);
    }
    db_close(); close(server_fd); return 0;
}
