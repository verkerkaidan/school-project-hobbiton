#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../src/auth_core.h"

int main(void) {
    if (db_init() != 0) { fprintf(stderr, "db_init failed (set SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD)\n"); return 2; }
    if (db_test_connect() != 0) { fprintf(stderr, "db_test_connect failed\n"); db_close(); return 2; }
    char email[128]; snprintf(email, sizeof(email), "test+%ld@example.com", (long)time(NULL));
    char id[37]; if (add_user_db(email, "TestPass1!", id, sizeof(id)) != 0) { fprintf(stderr, "add_user_db failed\n"); db_close(); return 2; }
    printf("created user id=%s email=%s\n", id, email);
    char found_id[37]; char hash[512]; char roles[512]; int enabled;
    if (find_user_by_email_db(email, found_id, sizeof(found_id), hash, sizeof(hash), roles, sizeof(roles), &enabled) != 0) { fprintf(stderr, "find_user_by_email_db failed\n"); db_close(); return 2; }
    printf("found id=%s roles=%s\n", found_id, roles);
    if (delete_user_by_id_db(found_id) != 0) { fprintf(stderr, "delete_user_by_id_db failed\n"); db_close(); return 2; }
    db_close(); printf("test_db: OK\n"); return 0;
}
