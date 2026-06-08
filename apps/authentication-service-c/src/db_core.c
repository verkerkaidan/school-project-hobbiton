/* db_core.c
 * PostgreSQL-backed user/role CRUD functions.
 */

#define _GNU_SOURCE
#include "auth_core.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <time.h>
#include <fcntl.h>

#if defined(__has_include)
#  if __has_include(<libpq-fe.h>)
#    include <libpq-fe.h>
#  elif __has_include(<postgresql/libpq-fe.h>)
#    include <postgresql/libpq-fe.h>
#  else
#    error "libpq-fe.h not found"
#  endif
#else
#  include <libpq-fe.h>
#endif

static PGconn *pg_conn = NULL;

static int migrate_schema(void);
static int exec_sql(const char *sql);

static int parse_jdbc_url(const char *url, char *host, size_t hostlen, char *port, size_t portlen, char *dbname, size_t dlen) {
    if (!url) return -1;
    const char *p = url;
    const char *prefix = "jdbc:postgresql://";
    if (strncmp(p, prefix, strlen(prefix)) == 0) p += strlen(prefix);

    const char *slash = strchr(p, '/');
    if (!slash) return -1;

    const char *hostport = p;
    size_t hostportlen = (size_t)(slash - p);
    const char *colon = memchr(hostport, ':', hostportlen);

    if (colon) {
        size_t hlen = (size_t)(colon - hostport);
        if (hlen >= hostlen) return -1;
        memcpy(host, hostport, hlen);
        host[hlen] = '\0';

        size_t plen = hostportlen - hlen - 1;
        if (plen >= portlen) return -1;
        memcpy(port, colon + 1, plen);
        port[plen] = '\0';
    } else {
        if (hostportlen >= hostlen) return -1;
        memcpy(host, hostport, hostportlen);
        host[hostportlen] = '\0';
        strncpy(port, "5432", portlen);
    }

    const char *q = slash + 1;
    const char *qend = strchr(q, '?');
    size_t dbnamelen = qend ? (size_t)(qend - q) : strlen(q);
    if (dbnamelen >= dlen) return -1;
    memcpy(dbname, q, dbnamelen);
    dbname[dbnamelen] = '\0';
    return 0;
}

int db_init(void) {
    const char *url = getenv("SPRING_DATASOURCE_URL");
    const char *user = getenv("SPRING_DATASOURCE_USERNAME");
    const char *pass = getenv("SPRING_DATASOURCE_PASSWORD");
    if (!url) url = getenv("DATABASE_URL");
    if (!url) {
        const char *pghost = getenv("PGHOST"); const char *pgport = getenv("PGPORT"); const char *pgdb = getenv("PGDATABASE");
        if (pghost && pgdb) {
            char conn[512]; snprintf(conn, sizeof(conn), "host=%s port=%s dbname=%s user=%s password=%s", pghost, pgport?pgport:"5432", pgdb, user?user:"", pass?pass:"");
            pg_conn = PQconnectdb(conn);
        } else {
            fprintf(stderr, "No SPRING_DATASOURCE_URL or PGHOST provided\n");
            return -1;
        }
    } else {
        char host[128] = ""; char port[16] = "5432"; char dbname[128] = "";
        if (parse_jdbc_url(url, host, sizeof(host), port, sizeof(port), dbname, sizeof(dbname)) != 0) {
            char conn[512]; snprintf(conn, sizeof(conn), "%s user=%s password=%s", url, user?user:"", pass?pass:"");
            pg_conn = PQconnectdb(conn);
        } else {
            char conn[512]; snprintf(conn, sizeof(conn), "host=%s port=%s dbname=%s user=%s password=%s", host, port, dbname, user?user:"", pass?pass:"");
            pg_conn = PQconnectdb(conn);
        }
    }
    if (!pg_conn) { fprintf(stderr, "PQconnectdb returned NULL\n"); return -1; }
    if (PQstatus(pg_conn) != CONNECTION_OK) { fprintf(stderr, "Connection to database failed: %s", PQerrorMessage(pg_conn)); PQfinish(pg_conn); pg_conn = NULL; return -1; }
    if (migrate_schema() != 0) {fprintf(stderr, "Schema migration failed\n"); PQfinish(pg_conn);pg_conn = NULL;return -1;}
    return 0;
}

void db_close(void) { if (pg_conn) { PQfinish(pg_conn); pg_conn = NULL; } }

int db_test_connect(void) { if (!pg_conn) return -1; PGresult *res = PQexec(pg_conn, "SELECT 1"); if (!res) return -1; if (PQresultStatus(res) != PGRES_TUPLES_OK) { PQclear(res); return -1; } PQclear(res); return 0; }

int find_user_by_email_db(const char *email, char *out_id, size_t idlen, char *out_hash, size_t hashlen, char *out_roles_csv, size_t roleslen, int *enabled) {
    if (!pg_conn) return -1;
    const char *paramValues[1] = { email };
    const char *sql = "SELECT cu.id::text, cu.email, cu.password, cu.enabled, COALESCE(string_agg(r.name, ','), '') AS roles FROM custom_user cu LEFT JOIN users_roles ur ON cu.id = ur.user_id LEFT JOIN role r ON ur.role_id = r.id WHERE cu.email = $1 GROUP BY cu.id, cu.email, cu.password, cu.enabled";
    PGresult *res = PQexecParams(pg_conn, sql, 1, NULL, paramValues, NULL, NULL, 0);
    if (!res) return -1;
    if (PQresultStatus(res) != PGRES_TUPLES_OK) { PQclear(res); return -1; }
    if (PQntuples(res) < 1) { PQclear(res); return -1; }
    char *id = PQgetvalue(res, 0, 0);
    char *password = PQgetvalue(res, 0, 2);
    char *roles = PQgetvalue(res, 0, 4);
    char *enabled_s = PQgetvalue(res, 0, 3);
    if (out_id && id) strncpy(out_id, id, idlen);
    if (out_hash && password) strncpy(out_hash, password, hashlen);
    if (out_roles_csv && roles) strncpy(out_roles_csv, roles, roleslen);
    if (enabled) *enabled = enabled_s ? atoi(enabled_s) : 1;
    PQclear(res);
    return 0;
}

int authenticate_user_db(const char *email, const char *password, char *out_id, size_t idlen, char *out_roles_csv, size_t roleslen) {
    if (!pg_conn) return -1;
    const char *paramValues[2] = { email, password };
    const char *sql = "SELECT cu.id::text, COALESCE(string_agg(r.name, ','), '') AS roles FROM custom_user cu LEFT JOIN users_roles ur ON cu.id = ur.user_id LEFT JOIN role r ON ur.role_id = r.id WHERE cu.email = $1 AND cu.password = crypt($2, cu.password) GROUP BY cu.id";
    PGresult *res = PQexecParams(pg_conn, sql, 2, NULL, paramValues, NULL, NULL, 0);
    if (!res) return -1;
    if (PQresultStatus(res) != PGRES_TUPLES_OK) { PQclear(res); return -1; }
    if (PQntuples(res) < 1) { PQclear(res); return -1; }
    if (out_id) strncpy(out_id, PQgetvalue(res, 0, 0), idlen);
    if (out_roles_csv) strncpy(out_roles_csv, PQgetvalue(res, 0, 1), roleslen);
    PQclear(res);
    return 0;
}

static char *ensure_role_and_get_id(const char *role_name) {
    if (!pg_conn) return NULL;
    const char *paramValues[1] = { role_name };
    PGresult *res = PQexecParams(pg_conn, "SELECT id::text FROM role WHERE name = $1", 1, NULL, paramValues, NULL, NULL, 0);
    if (!res) return NULL;
    if (PQresultStatus(res) == PGRES_TUPLES_OK && PQntuples(res) >= 1) { char *rid = strdup(PQgetvalue(res, 0, 0)); PQclear(res); return rid; }
    PQclear(res);
    PGresult *ins = PQexecParams(pg_conn, "INSERT INTO role (name) VALUES ($1) RETURNING id::text", 1, NULL, paramValues, NULL, NULL, 0);
    if (!ins) return NULL;
    if (PQresultStatus(ins) == PGRES_TUPLES_OK && PQntuples(ins) >= 1) { char *rid = strdup(PQgetvalue(ins, 0, 0)); PQclear(ins); return rid; }
    PQclear(ins); return NULL;
}

int add_user_db_with_hash(const char *email, const char *hash, const char *roles_csv, char *out_id, size_t idlen) {
    if (!pg_conn) return -1;
    char id[37]; unsigned char rnd[16];
    // simple uuid v4 generation
    int fd = open("/dev/urandom", O_RDONLY);
    if (fd < 0) return -1;
    if (read(fd, rnd, sizeof(rnd)) != sizeof(rnd)) { close(fd); return -1; }
    close(fd);
    snprintf(id, sizeof(id), "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
             rnd[0],rnd[1],rnd[2],rnd[3],rnd[4],rnd[5],(rnd[6]&0x0F)|0x40,rnd[7],(rnd[8]&0x3F)|0x80,rnd[9],rnd[10],rnd[11],rnd[12],rnd[13],rnd[14],rnd[15]);
    const char *params[4]; params[0] = id; params[1] = email; params[2] = hash; params[3] = "t";
    PGresult *res = PQexecParams(pg_conn, "BEGIN", 0, NULL, NULL, NULL, NULL, 0); if (!res) return -1; PQclear(res);
    const char *sql = "INSERT INTO custom_user (id, email, password, enabled) VALUES ($1::uuid, $2, $3, $4)";
    PGresult *ins = PQexecParams(pg_conn, sql, 4, NULL, params, NULL, NULL, 0);
    if (!ins) { PQexec(pg_conn, "ROLLBACK"); return -1; }
    if (PQresultStatus(ins) != PGRES_COMMAND_OK) { PQclear(ins); PQexec(pg_conn, "ROLLBACK"); return -1; }
    PQclear(ins);
    char role_buf[256]; if (!roles_csv || strlen(roles_csv)==0) strncpy(role_buf, "ROLE_USER", sizeof(role_buf)); else { strncpy(role_buf, roles_csv, sizeof(role_buf)); char *comma = strchr(role_buf, ','); if (comma) *comma = '\0'; }
    char *rid = ensure_role_and_get_id(role_buf);
    if (!rid) { PQexec(pg_conn, "ROLLBACK"); return -1; }
    const char *params2[2]; params2[0] = id; params2[1] = rid;
    PGresult *ins2 = PQexecParams(pg_conn, "INSERT INTO users_roles (user_id, role_id) VALUES ($1::uuid, $2::bigint)", 2, NULL, params2, NULL, NULL, 0);
    if (!ins2) { free(rid); PQexec(pg_conn, "ROLLBACK"); return -1; }
    if (PQresultStatus(ins2) != PGRES_COMMAND_OK) { PQclear(ins2); free(rid); PQexec(pg_conn, "ROLLBACK"); return -1; }
    PQclear(ins2); free(rid);
    PGresult *comm = PQexec(pg_conn, "COMMIT"); if (!comm) return -1; PQclear(comm);
    if (out_id) strncpy(out_id, id, idlen);
    return 0;
}

int add_user_db(const char *email, const char *password, char *out_id, size_t idlen) {
    if (!pg_conn) return -1;
    // Use PostgreSQL's pgcrypto to generate bcrypt hash server-side: crypt($1, gen_salt('bf', 12))
    char id[37]; unsigned char rnd[16];
    int fd = open("/dev/urandom", O_RDONLY);
    if (fd < 0) return -1;
    if (read(fd, rnd, sizeof(rnd)) != sizeof(rnd)) { close(fd); return -1; }
    close(fd);
    snprintf(id, sizeof(id), "%02x%02x%02x%02x-%02x%02x-%02x%02x-%02x%02x-%02x%02x%02x%02x%02x%02x",
             rnd[0],rnd[1],rnd[2],rnd[3],rnd[4],rnd[5],(rnd[6]&0x0F)|0x40,rnd[7],(rnd[8]&0x3F)|0x80,rnd[9],rnd[10],rnd[11],rnd[12],rnd[13],rnd[14],rnd[15]);
    const char *params[4]; params[0] = id; params[1] = email; params[2] = password; params[3] = "t";
    PGresult *res = PQexecParams(pg_conn, "BEGIN", 0, NULL, NULL, NULL, NULL, 0); if (!res) return -1; PQclear(res);
    const char *sql = "INSERT INTO custom_user (id, email, password, enabled) VALUES ($1::uuid, $2, crypt($3, gen_salt('bf', 12)), $4)";
    PGresult *ins = PQexecParams(pg_conn, sql, 4, NULL, params, NULL, NULL, 0);
    if (!ins) { PQexec(pg_conn, "ROLLBACK"); return -1; }
    if (PQresultStatus(ins) != PGRES_COMMAND_OK) { PQclear(ins); PQexec(pg_conn, "ROLLBACK"); return -1; }
    PQclear(ins);
    // assign default role ROLE_USER
    char role_buf[256]; strncpy(role_buf, "ROLE_USER", sizeof(role_buf));
    char *rid = ensure_role_and_get_id(role_buf);
    if (!rid) { PQexec(pg_conn, "ROLLBACK"); return -1; }
    const char *params2[2]; params2[0] = id; params2[1] = rid;
    PGresult *ins2 = PQexecParams(pg_conn, "INSERT INTO users_roles (user_id, role_id) VALUES ($1::uuid, $2::bigint)", 2, NULL, params2, NULL, NULL, 0);
    if (!ins2) { free(rid); PQexec(pg_conn, "ROLLBACK"); return -1; }
    if (PQresultStatus(ins2) != PGRES_COMMAND_OK) { PQclear(ins2); free(rid); PQexec(pg_conn, "ROLLBACK"); return -1; }
    PQclear(ins2); free(rid);
    PGresult *comm = PQexec(pg_conn, "COMMIT"); if (!comm) return -1; PQclear(comm);
    if (out_id) strncpy(out_id, id, idlen);
    return 0;
}

int delete_user_by_id_db(const char *id) {
    if (!pg_conn) return -1;
    const char *params[1] = { id };
    PGresult *res = PQexecParams(pg_conn, "BEGIN", 0, NULL, NULL, NULL, NULL, 0); if (!res) return -1; PQclear(res);
    PGresult *r1 = PQexecParams(pg_conn, "DELETE FROM users_roles WHERE user_id = $1::uuid", 1, NULL, params, NULL, NULL, 0);
    if (!r1) { PQexec(pg_conn, "ROLLBACK"); return -1; } PQclear(r1);
    PGresult *r2 = PQexecParams(pg_conn, "DELETE FROM custom_user WHERE id = $1::uuid", 1, NULL, params, NULL, NULL, 0);
    if (!r2) { PQexec(pg_conn, "ROLLBACK"); return -1; } PQclear(r2);
    PGresult *comm = PQexec(pg_conn, "COMMIT"); if (!comm) return -1; PQclear(comm);
    return 0;
}

int update_user_role_db(const char *id, const char *role_name) {
    if (!pg_conn) return -1;
    char *rid = ensure_role_and_get_id(role_name); if (!rid) return -1;
    const char *params[1] = { id };
    PGresult *res = PQexecParams(pg_conn, "BEGIN", 0, NULL, NULL, NULL, NULL, 0); if (!res) { free(rid); return -1; } PQclear(res);
    PGresult *d = PQexecParams(pg_conn, "DELETE FROM users_roles WHERE user_id = $1::uuid", 1, NULL, params, NULL, NULL, 0);
    if (!d) { free(rid); PQexec(pg_conn, "ROLLBACK"); return -1; } PQclear(d);
    const char *params2[2] = { id, rid };
    PGresult *ins = PQexecParams(pg_conn, "INSERT INTO users_roles (user_id, role_id) VALUES ($1::uuid, $2::bigint)", 2, NULL, params2, NULL, NULL, 0);
    if (!ins) { free(rid); PQexec(pg_conn, "ROLLBACK"); return -1; }
    if (PQresultStatus(ins) != PGRES_COMMAND_OK) { free(rid); PQclear(ins); PQexec(pg_conn, "ROLLBACK"); return -1; }
    PQclear(ins); free(rid);
    PGresult *comm = PQexec(pg_conn, "COMMIT"); if (!comm) return -1; PQclear(comm);
    return 0;
}

static int exec_sql(const char *sql)
{
    PGresult *res = PQexec(pg_conn, sql);

    if (!res) {
        fprintf(stderr, "Migration failed: PQexec returned NULL\n");
        return -1;
    }

    ExecStatusType status = PQresultStatus(res);

    if (status != PGRES_COMMAND_OK &&
        status != PGRES_TUPLES_OK)
    {
        fprintf(stderr,
                "Migration failed:\n%s\n%s\n",
                sql,
                PQerrorMessage(pg_conn));

        PQclear(res);
        return -1;
    }

    PQclear(res);
    return 0;
}

static int migrate_schema(void)
{
    if (!pg_conn)
        return -1;

    fprintf(stderr, "Running authentication schema migration...\n");

    /* --------------------------------------------------
     * pgcrypto
     * -------------------------------------------------- */
    if (exec_sql(
        "CREATE EXTENSION IF NOT EXISTS pgcrypto;"
    ) != 0)
        return -1;

    /* --------------------------------------------------
     * Preserve legacy tables if they still exist
     * -------------------------------------------------- */
    if (exec_sql(
        "DO $$ "
        "BEGIN "
        "  IF EXISTS ( "
        "    SELECT 1 "
        "    FROM information_schema.tables "
        "    WHERE table_schema='public' "
        "      AND table_name='privileges' "
        "  ) "
        "  AND NOT EXISTS ( "
        "    SELECT 1 "
        "    FROM information_schema.tables "
        "    WHERE table_schema='public' "
        "      AND table_name='privileges_legacy' "
        "  ) "
        "  THEN "
        "    ALTER TABLE privileges "
        "    RENAME TO privileges_legacy; "
        "  END IF; "
        "END $$;"
    ) != 0)
        return -1;

    if (exec_sql(
        "DO $$ "
        "BEGIN "
        "  IF EXISTS ( "
        "    SELECT 1 "
        "    FROM information_schema.tables "
        "    WHERE table_schema='public' "
        "      AND table_name='roles_privileges' "
        "  ) "
        "  AND NOT EXISTS ( "
        "    SELECT 1 "
        "    FROM information_schema.tables "
        "    WHERE table_schema='public' "
        "      AND table_name='roles_privileges_legacy' "
        "  ) "
        "  THEN "
        "    ALTER TABLE roles_privileges "
        "    RENAME TO roles_privileges_legacy; "
        "  END IF; "
        "END $$;"
    ) != 0)
        return -1;

    /* --------------------------------------------------
     * Core auth tables
     * -------------------------------------------------- */
    if (exec_sql(
        "CREATE TABLE IF NOT EXISTS custom_user ("
        "    id UUID PRIMARY KEY,"
        "    email TEXT UNIQUE NOT NULL,"
        "    password TEXT NOT NULL,"
        "    enabled BOOLEAN DEFAULT TRUE"
        ");"
    ) != 0)
        return -1;

    if (exec_sql(
        "CREATE TABLE IF NOT EXISTS role ("
        "    id BIGSERIAL PRIMARY KEY,"
        "    name TEXT UNIQUE NOT NULL"
        ");"
    ) != 0)
        return -1;

    if (exec_sql(
        "CREATE TABLE IF NOT EXISTS users_roles ("
        "    user_id UUID NOT NULL,"
        "    role_id BIGINT NOT NULL,"
        "    PRIMARY KEY(user_id, role_id),"
        "    CONSTRAINT fk_users_roles_user "
        "        FOREIGN KEY(user_id) "
        "        REFERENCES custom_user(id) "
        "        ON DELETE CASCADE,"
        "    CONSTRAINT fk_users_roles_role "
        "        FOREIGN KEY(role_id) "
        "        REFERENCES role(id) "
        "        ON DELETE CASCADE"
        ");"
    ) != 0)
        return -1;

    /* --------------------------------------------------
     * Seed roles
     * -------------------------------------------------- */
    if (exec_sql(
        "INSERT INTO role(name) "
        "SELECT 'ROLE_USER' "
        "WHERE NOT EXISTS ("
        "    SELECT 1 FROM role WHERE name = 'ROLE_USER'"
        ");"
    ) != 0)
        return -1;

    if (exec_sql(
        "INSERT INTO role(name) "
        "SELECT 'ROLE_ADMIN' "
        "WHERE NOT EXISTS ("
        "    SELECT 1 FROM role WHERE name = 'ROLE_ADMIN'"
        ");"
    ) != 0)
        return -1;

    fprintf(stderr, "Authentication schema migration complete.\n");

    return 0;
}
