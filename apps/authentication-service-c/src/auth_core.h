#pragma once

#include <stddef.h>

int db_init(void);
void db_close(void);
extern char *g_conninfo;

/* Find a user by email. Buffers must have sufficient size. Returns 0 on found, -1 on not found/error */
int find_user_by_email_db(const char *email, char *out_id, size_t idlen, char *out_hash, size_t hashlen, char *out_roles_csv, size_t roleslen, int *enabled);

/* Add user using provided bcrypt-hashed password. If out_id non-NULL, it will receive new user's id (37 bytes including null). Returns 0 on success */
int add_user_db_with_hash(const char *email, const char *hash, const char *roles_csv, char *out_id, size_t idlen);

/* Add user by providing plaintext password (will be hashed). Returns 0 on success */
int add_user_db(const char *email, const char *password, char *out_id, size_t idlen);

int delete_user_by_id_db(const char *id);
int update_user_role_db(const char *id, const char *role_name);
int update_password_db(const char *id, const char *current_password, const char *new_password);
int update_email_db(const char *id, const char *new_email);

/* Authenticate user using plaintext password against DB-stored bcrypt hash.
   Returns 0 on success and fills out_id and out_roles_csv. */
int authenticate_user_db(const char *email, const char *password, char *out_id, size_t idlen, char *out_roles_csv, size_t roleslen);

/* JWT helpers */
char *jwt_generate(const char *userId, const char *roles_csv);
int jwt_validate_and_extract(const char *token, char **out_userId, char **out_roles_csv);

/* Utility */
int is_valid_email(const char *email);
int is_valid_password(const char *password);

/* For tests */
int db_test_connect(void);
