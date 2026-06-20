#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <time.h>
#include "../src/auth_core.h"

static int tests_run = 0;
static int tests_passed = 0;
static int tests_failed = 0;

#define ASSERT(cond, msg) do { \
    tests_run++; \
    if (!(cond)) { \
        fprintf(stderr, "FAIL [line %d]: %s\n", __LINE__, msg); \
        tests_failed++; \
    } else { \
        tests_passed++; \
    } \
} while(0)

static void test_jwt_generate_and_validate(void) {
    char *token = jwt_generate("550e8400-e29b-41d4-a716-446655440000", "ROLE_USER");
    ASSERT(token != NULL, "jwt_generate should succeed");
    if (!token) return;

    char *userId = NULL, *roles = NULL;
    int rc = jwt_validate_and_extract(token, &userId, &roles);
    ASSERT(rc == 0, "jwt_validate_and_extract should return 0");
    ASSERT(userId != NULL, "userId should be extracted");
    ASSERT(strcmp(userId, "550e8400-e29b-41d4-a716-446655440000") == 0, "userId should match");
    ASSERT(roles != NULL, "roles should be extracted");
    ASSERT(strcmp(roles, "ROLE_USER") == 0, "roles should be ROLE_USER");

    free(token);
    free(userId);
    free(roles);
}

static void test_jwt_multiple_roles(void) {
    char *token = jwt_generate("user-abc", "ROLE_USER,ROLE_ADMIN,ROLE_STAFF");
    ASSERT(token != NULL, "jwt_generate with multiple roles should succeed");
    if (!token) return;

    char *userId = NULL, *roles = NULL;
    int rc = jwt_validate_and_extract(token, &userId, &roles);
    ASSERT(rc == 0, "validate with multiple roles should succeed");
    ASSERT(userId != NULL, "userId should be extracted");
    ASSERT(roles != NULL, "roles should be extracted");

    int has_user = strstr(roles, "ROLE_USER") != NULL;
    int has_admin = strstr(roles, "ROLE_ADMIN") != NULL;
    int has_staff = strstr(roles, "ROLE_STAFF") != NULL;
    ASSERT(has_user, "should contain ROLE_USER");
    ASSERT(has_admin, "should contain ROLE_ADMIN");
    ASSERT(has_staff, "should contain ROLE_STAFF");

    free(token);
    free(userId);
    free(roles);
}

static void test_jwt_tampered_signature(void) {
    char *token = jwt_generate("user-123", "ROLE_USER");
    ASSERT(token != NULL, "should generate token");
    if (!token) return;

    /* Tamper the payload by flipping a byte */
    char *dot = strchr(token, '.');
    ASSERT(dot != NULL, "should have first dot");
    if (!dot) { free(token); return; }
    dot = strchr(dot + 1, '.');
    ASSERT(dot != NULL, "should have second dot");
    if (!dot) { free(token); return; }

    /* flip a bit in the signature part */
    dot++;
    if (*dot) *dot ^= 0x01;

    char *userId = NULL, *roles = NULL;
    int rc = jwt_validate_and_extract(token, &userId, &roles);
    ASSERT(rc != 0, "tampered token should fail validation");

    free(token);
    free(userId);
    free(roles);
}

static void test_jwt_malformed_token(void) {
    char *userId = NULL, *roles = NULL;
    int rc = jwt_validate_and_extract("not-a-jwt", &userId, &roles);
    ASSERT(rc != 0, "malformed token should fail");
    free(userId);
    free(roles);

    rc = jwt_validate_and_extract("header.payload", &userId, &roles);
    ASSERT(rc != 0, "token without signature should fail");
    free(userId);
    free(roles);

    rc = jwt_validate_and_extract("", &userId, &roles);
    ASSERT(rc != 0, "empty token should fail");
    free(userId);
    free(roles);
}

static void test_jwt_empty_roles(void) {
    char *token = jwt_generate("user-no-role", "");
    ASSERT(token != NULL, "jwt_generate with empty roles should succeed");
    if (!token) return;

    char *userId = NULL, *roles = NULL;
    int rc = jwt_validate_and_extract(token, &userId, &roles);
    ASSERT(rc == 0, "validate with empty roles should succeed");
    ASSERT(userId != NULL, "userId should be extracted");

    free(token);
    free(userId);
    free(roles);
}

static void test_email_validation(void) {
    ASSERT(is_valid_email("user@example.com"), "user@example.com valid");
    ASSERT(is_valid_email("a@b.co"), "a@b.co valid");
    ASSERT(is_valid_email("test.user+tag@domain.co.uk"), "test.user+tag@domain.co.uk valid");

    ASSERT(!is_valid_email(NULL), "NULL email invalid");
    ASSERT(!is_valid_email(""), "empty email invalid");
    ASSERT(!is_valid_email("no-at-sign"), "no @ invalid");
    ASSERT(!is_valid_email("@nouser.com"), "@nouser.com invalid (no local part)");
    ASSERT(!is_valid_email("user@"), "user@ invalid (no domain)");
}

static void test_password_validation(void) {
    ASSERT(is_valid_password("Aa1!test!"), "valid password with all requirements");
    ASSERT(is_valid_password("Str0ng!Pass"), "Str0ng!Pass valid");

    ASSERT(!is_valid_password(NULL), "NULL password invalid");
    ASSERT(!is_valid_password(""), "empty password invalid");
    ASSERT(!is_valid_password("Ab1!"), "too short (< 8) invalid");
    ASSERT(!is_valid_password("abcdefgh"), "no digit, no upper, no special invalid");
    ASSERT(!is_valid_password("ABCDEFGH1!"), "no lowercase invalid");
    ASSERT(!is_valid_password("abcdefgh1!"), "no uppercase invalid");
    ASSERT(!is_valid_password("Abcdefgh!"), "no digit invalid");
    ASSERT(!is_valid_password("Abcdefgh1"), "no special char invalid");
    ASSERT(!is_valid_password("Abc def1!"), "contains space invalid");
}

int main(void) {
    setenv("JWT_SECRET", "test-secret", 1);
    setenv("JWT_ISSUER", "test-issuer", 1);

    printf("=== JWT tests ===\n");
    test_jwt_generate_and_validate();
    test_jwt_multiple_roles();
    test_jwt_tampered_signature();
    test_jwt_malformed_token();
    test_jwt_empty_roles();

    printf("=== Email validation tests ===\n");
    test_email_validation();

    printf("=== Password validation tests ===\n");
    test_password_validation();

    printf("\nResults: %d/%d passed, %d failed\n",
           tests_passed, tests_run, tests_failed);

    return tests_failed > 0 ? 1 : 0;
}
