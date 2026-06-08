#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include "../src/auth_core.h"

int main(void) {
    char *token = jwt_generate("user-123", "ROLE_USER");
    if (!token) { fprintf(stderr, "jwt_generate failed\n"); return 2; }
    printf("token=%s\n", token);
    char *userId = NULL; char *roles = NULL;
    if (jwt_validate_and_extract(token, &userId, &roles) != 0) { fprintf(stderr, "jwt_validate_and_extract failed\n"); free(token); return 2; }
    if (!userId) { fprintf(stderr, "no userId extracted\n"); free(token); return 2; }
    printf("extracted userId=%s roles=%s\n", userId, roles ? roles : "(null)");
    free(token); free(userId); free(roles);
    printf("test_jwt: OK\n");
    return 0;
}
