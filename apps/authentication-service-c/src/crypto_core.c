/* crypto_core.c
 * Crypto and JWT helpers, bcrypt helpers and validators.
 * No OpenSSL dependency.
 */

#define _GNU_SOURCE
#include "auth_core.h"
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <time.h>
#include <unistd.h>
#include <fcntl.h>
#include <pthread.h>

/* crypt: bcrypt support */
#if defined(__has_include)
#  if __has_include(<crypt.h>)
#    include <crypt.h>
#    define HAVE_CRYPT_R 1
#  endif
#endif
#ifndef HAVE_CRYPT_R
#  undef HAVE_CRYPT_R
#endif

#ifndef HAVE_CRYPT_R
/* If crypt.h not available, provide fallback prototype so compilation succeeds.
   We will use crypt() (not crypt_r) guarded by a mutex at runtime. */
extern char *crypt(const char *key, const char *salt);
#endif

/* SHA256 implementation */
typedef struct {
    unsigned int state[8];
    unsigned long long bitlen;
    unsigned char data[64];
    unsigned int datalen;
} SHA256_CTX;

static const unsigned int k[64] = {
    0x428a2f98,0x71374491,0xb5c0fbcf,0xe9b5dba5,0x3956c25b,0x59f111f1,0x923f82a4,0xab1c5ed5,
    0xd807aa98,0x12835b01,0x243185be,0x550c7dc3,0x72be5d74,0x80deb1fe,0x9bdc06a7,0xc19bf174,
    0xe49b69c1,0xefbe4786,0x0fc19dc6,0x240ca1cc,0x2de92c6f,0x4a7484aa,0x5cb0a9dc,0x76f988da,
    0x983e5152,0xa831c66d,0xb00327c8,0xbf597fc7,0xc6e00bf3,0xd5a79147,0x06ca6351,0x14292967,
    0x27b70a85,0x2e1b2138,0x4d2c6dfc,0x53380d13,0x650a7354,0x766a0abb,0x81c2c92e,0x92722c85,
    0xa2bfe8a1,0xa81a664b,0xc24b8b70,0xc76c51a3,0xd192e819,0xd6990624,0xf40e3585,0x106aa070,
    0x19a4c116,0x1e376c08,0x2748774c,0x34b0bcb5,0x391c0cb3,0x4ed8aa4a,0x5b9cca4f,0x682e6ff3,
    0x748f82ee,0x78a5636f,0x84c87814,0x8cc70208,0x90befffa,0xa4506ceb,0xbef9a3f7,0xc67178f2
};

static void sha256_transform(SHA256_CTX *ctx, const unsigned char data[]) {
    unsigned int m[64];
    unsigned int a,b,c,d,e,f,g,h,i,t1,t2;
    for (i = 0; i < 16; ++i) {
        m[i] = (data[i*4] << 24) | (data[i*4+1] << 16) | (data[i*4+2] << 8) | (data[i*4+3]);
    }
    for ( ; i < 64; ++i) {
        unsigned int s0 = ( (m[i-15] >> 7) | (m[i-15] << (32-7)) ) ^ ( (m[i-15] >> 18) | (m[i-15] << (32-18)) ) ^ (m[i-15] >> 3);
        unsigned int s1 = ( (m[i-2] >> 17) | (m[i-2] << (32-17)) ) ^ ( (m[i-2] >> 19) | (m[i-2] << (32-19)) ) ^ (m[i-2] >> 10);
        m[i] = m[i-16] + s0 + m[i-7] + s1;
    }
    a = ctx->state[0]; b = ctx->state[1]; c = ctx->state[2]; d = ctx->state[3];
    e = ctx->state[4]; f = ctx->state[5]; g = ctx->state[6]; h = ctx->state[7];
    for (i = 0; i < 64; ++i) {
        unsigned int S1 = ((e >> 6) | (e << (32-6))) ^ ((e >> 11) | (e << (32-11))) ^ ((e >> 25) | (e << (32-25)));
        unsigned int ch = (e & f) ^ ((~e) & g);
        t1 = h + S1 + ch + k[i] + m[i];
        unsigned int S0 = ((a >> 2) | (a << (32-2))) ^ ((a >> 13) | (a << (32-13))) ^ ((a >> 22) | (a << (32-22)));
        unsigned int maj = (a & b) ^ (a & c) ^ (b & c);
        t2 = S0 + maj;
        h = g; g = f; f = e; e = d + t1; d = c; c = b; b = a; a = t1 + t2;
    }
    ctx->state[0] += a; ctx->state[1] += b; ctx->state[2] += c; ctx->state[3] += d;
    ctx->state[4] += e; ctx->state[5] += f; ctx->state[6] += g; ctx->state[7] += h;
}

static void sha256_init(SHA256_CTX *ctx) {
    ctx->datalen = 0; ctx->bitlen = 0;
    ctx->state[0] = 0x6a09e667; ctx->state[1] = 0xbb67ae85; ctx->state[2] = 0x3c6ef372; ctx->state[3] = 0xa54ff53a;
    ctx->state[4] = 0x510e527f; ctx->state[5] = 0x9b05688c; ctx->state[6] = 0x1f83d9ab; ctx->state[7] = 0x5be0cd19;
}

static void sha256_update(SHA256_CTX *ctx, const unsigned char data[], size_t len) {
    unsigned int i;
    for (i = 0; i < len; ++i) {
        ctx->data[ctx->datalen] = data[i]; ctx->datalen++;
        if (ctx->datalen == 64) {
            sha256_transform(ctx, ctx->data);
            ctx->bitlen += 512; ctx->datalen = 0;
        }
    }
}

static void sha256_final(SHA256_CTX *ctx, unsigned char hash[]) {
    unsigned int i = ctx->datalen;
    if (ctx->datalen < 56) {
        ctx->data[i++] = 0x80;
        while (i < 56) ctx->data[i++] = 0x00;
    } else {
        ctx->data[i++] = 0x80;
        while (i < 64) ctx->data[i++] = 0x00;
        sha256_transform(ctx, ctx->data);
        memset(ctx->data, 0, 56);
    }
    ctx->bitlen += ctx->datalen * 8;
    ctx->data[63] = ctx->bitlen;
    ctx->data[62] = ctx->bitlen >> 8;
    ctx->data[61] = ctx->bitlen >> 16;
    ctx->data[60] = ctx->bitlen >> 24;
    ctx->data[59] = ctx->bitlen >> 32;
    ctx->data[58] = ctx->bitlen >> 40;
    ctx->data[57] = ctx->bitlen >> 48;
    ctx->data[56] = ctx->bitlen >> 56;
    sha256_transform(ctx, ctx->data);
    for (i = 0; i < 4; ++i) {
        hash[i]      = (ctx->state[0] >> (24 - i * 8)) & 0x000000ff;
        hash[i + 4]  = (ctx->state[1] >> (24 - i * 8)) & 0x000000ff;
        hash[i + 8]  = (ctx->state[2] >> (24 - i * 8)) & 0x000000ff;
        hash[i + 12] = (ctx->state[3] >> (24 - i * 8)) & 0x000000ff;
        hash[i + 16] = (ctx->state[4] >> (24 - i * 8)) & 0x000000ff;
        hash[i + 20] = (ctx->state[5] >> (24 - i * 8)) & 0x000000ff;
        hash[i + 24] = (ctx->state[6] >> (24 - i * 8)) & 0x000000ff;
        hash[i + 28] = (ctx->state[7] >> (24 - i * 8)) & 0x000000ff;
    }
}

static void hmac_sha256(const unsigned char *key, size_t keylen, const unsigned char *data, size_t datalen, unsigned char *out) {
    unsigned char k_ipad[64];
    unsigned char k_opad[64];
    unsigned char tk[32];
    size_t i;
    if (keylen > 64) {
        SHA256_CTX tctx; sha256_init(&tctx); sha256_update(&tctx, key, keylen); sha256_final(&tctx, tk);
        key = tk; keylen = 32;
    }
    memset(k_ipad, 0, sizeof(k_ipad)); memset(k_opad, 0, sizeof(k_opad));
    memcpy(k_ipad, key, keylen); memcpy(k_opad, key, keylen);
    for (i = 0; i < 64; ++i) { k_ipad[i] ^= 0x36; k_opad[i] ^= 0x5c; }
    SHA256_CTX ctx;
    sha256_init(&ctx); sha256_update(&ctx, k_ipad, 64); sha256_update(&ctx, data, datalen);
    unsigned char inner_hash[32]; sha256_final(&ctx, inner_hash);
    sha256_init(&ctx); sha256_update(&ctx, k_opad, 64); sha256_update(&ctx, inner_hash, 32);
    sha256_final(&ctx, out);
}

static const char b64chars[] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";

static char *base64_encode(const unsigned char *in, size_t len) {
    size_t outlen = 4 * ((len + 2) / 3);
    char *out = malloc(outlen + 1);
    if (!out) return NULL;
    size_t i, j = 0;
    for (i = 0; i < len;) {
        unsigned int octet_a = i < len ? in[i++] : 0;
        unsigned int octet_b = i < len ? in[i++] : 0;
        unsigned int octet_c = i < len ? in[i++] : 0;
        unsigned int triple = (octet_a << 16) | (octet_b << 8) | octet_c;
        out[j++] = b64chars[(triple >> 18) & 0x3F];
        out[j++] = b64chars[(triple >> 12) & 0x3F];
        out[j++] = b64chars[(triple >> 6) & 0x3F];
        out[j++] = b64chars[triple & 0x3F];
    }
    for (i = 0; i < (3 - (len % 3)) % 3; ++i) out[outlen - 1 - i] = '=';
    out[outlen] = '\0';
    return out;
}

static char *base64url_encode(const unsigned char *in, size_t len) {
    char *b64 = base64_encode(in, len);
    if (!b64) return NULL;
    for (char *p = b64; *p; ++p) {
        if (*p == '+') *p = '-';
        else if (*p == '/') *p = '_';
    }
    size_t L = strlen(b64);
    while (L > 0 && b64[L-1] == '=') { b64[L-1] = '\0'; --L; }
    return b64;
}

static int base64val(char c) {
    if (c >= 'A' && c <= 'Z') return c - 'A';
    if (c >= 'a' && c <= 'z') return c - 'a' + 26;
    if (c >= '0' && c <= '9') return c - '0' + 52;
    if (c == '+') return 62;
    if (c == '/') return 63;
    return -1;
}

static unsigned char *base64_decode_alloc(const char *in, size_t *outlen) {
    size_t len = strlen(in);
    size_t out_capacity = (len * 3) / 4 + 4;
    unsigned char *out = malloc(out_capacity);
    if (!out) return NULL;
    unsigned int val = 0; int valb = -8; size_t idx = 0;
    for (size_t i = 0; i < len; ++i) {
        char c = in[i]; if (c == '-' ) c = '+'; else if (c == '_') c = '/';
        int d = base64val(c);
        if (d == -1) break;
        val = (val << 6) | d; valb += 6;
        if (valb >= 0) { out[idx++] = (val >> valb) & 0xFF; valb -= 8; }
    }
    *outlen = idx; return out;
}

static char *JWT_SECRET = NULL;
static char *JWT_ISSUER = NULL;

char *jwt_generate(const char *userId, const char *roles_csv) {
    if (!JWT_SECRET)
        JWT_SECRET = getenv("JWT_SECRET");
    if (!JWT_SECRET)
        JWT_SECRET = "your-secret";
    if (!JWT_ISSUER)
        JWT_ISSUER = getenv("JWT_ISSUER");

    if (!JWT_ISSUER)
        JWT_ISSUER = "auth-service";
    time_t now = time(NULL);
    time_t exp = now + 6*60*60;
    char roles_json[512] = "[";
    char tmp[128]; const char *p = roles_csv; int first = 1;
    while (p && *p) {
        const char *comma = strchr(p, ','); size_t len = comma ? (size_t)(comma - p) : strlen(p);
        if (!first) strncat(roles_json, ",", sizeof(roles_json)-strlen(roles_json)-1);
        snprintf(tmp, sizeof(tmp), "\"%.*s\"", (int)len, p);
        strncat(roles_json, tmp, sizeof(roles_json)-strlen(roles_json)-1);
        first = 0; if (!comma) break; p = comma + 1;
    }
    strncat(roles_json, "]", sizeof(roles_json)-strlen(roles_json)-1);
    char payload[1024]; snprintf(payload, sizeof(payload), "{\"sub\":\"User Details\",\"userId\":\"%s\",\"role\":%s,\"iat\":%lld,\"exp\":%lld,\"iss\":\"%s\"}", userId, roles_json, (long long)now, (long long)exp, JWT_ISSUER);
    const char *header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
    char *h_enc = base64url_encode((const unsigned char*)header, strlen(header));
    char *p_enc = base64url_encode((const unsigned char*)payload, strlen(payload));
    if (!h_enc || !p_enc) { free(h_enc); free(p_enc); return NULL; }
    size_t msglen = strlen(h_enc) + 1 + strlen(p_enc);
    char *msg = malloc(msglen + 1); snprintf(msg, msglen+1, "%s.%s", h_enc, p_enc);
    unsigned char sig[32]; hmac_sha256((unsigned char*)JWT_SECRET, strlen(JWT_SECRET), (unsigned char*)msg, strlen(msg), sig);
    char *sig_enc = base64url_encode(sig, 32);
    if (!sig_enc) { free(h_enc); free(p_enc); free(msg); return NULL; }
    size_t token_len = strlen(msg) + 1 + strlen(sig_enc);
    char *token = malloc(token_len + 1); snprintf(token, token_len+1, "%s.%s", msg, sig_enc);
    free(h_enc); free(p_enc); free(sig_enc); free(msg); return token;
}

static char *json_get_string(const char *json, const char *key) { char pattern[128]; snprintf(pattern, sizeof(pattern), "\"%s\"", key); char *p = strstr(json, pattern); if (!p) return NULL; p = strchr(p, ':'); if (!p) return NULL; p++; while (*p && isspace((unsigned char)*p)) p++; if (*p != '"') return NULL; p++; const char *start = p; while (*p && *p != '"') { if (*p == '\\' && *(p+1)) p += 2; else p++; } size_t len = p - start; char *out = malloc(len + 1); if (!out) return NULL; strncpy(out, start, len); out[len] = '\0'; return out; }
static long long json_get_int(const char *json, const char *key, long long fallback) { char pattern[128]; snprintf(pattern, sizeof(pattern), "\"%s\"", key); char *p = strstr(json, pattern); if (!p) return fallback; p = strchr(p, ':'); if (!p) return fallback; p++; while (*p && isspace((unsigned char)*p)) p++; char *endptr; long long val = strtoll(p, &endptr, 10); if (p == endptr) return fallback; return val; }
static char *json_get_roles_csv(const char *json) { char *p = strstr(json, "\"role\""); if (!p) return NULL; p = strchr(p, ':'); if (!p) return NULL; p++; while (*p && isspace((unsigned char)*p)) p++; if (*p != '[') return NULL; p++; char roles[512] = ""; int first = 1; while (*p && *p != ']') { while (*p && isspace((unsigned char)*p)) p++; if (*p == '"') { p++; const char *start = p; while (*p && *p != '"') { if (*p == '\\' && *(p+1)) p += 2; else p++; } size_t len = p - start; if (!first) strncat(roles, ",", sizeof(roles)-strlen(roles)-1); char tmp[256]; snprintf(tmp, sizeof(tmp), "%.*s", (int)len, start); strncat(roles, tmp, sizeof(roles)-strlen(roles)-1); first = 0; if (*p == '"') p++; } else p++; while (*p && *p != ']' && *p != '"') p++; } if (strlen(roles) == 0) return NULL; return strdup(roles); }

int jwt_validate_and_extract(const char *token, char **out_userId, char **out_roles_csv) { if (!JWT_SECRET) JWT_SECRET = getenv("JWT_SECRET"); if (!JWT_SECRET) JWT_SECRET = "your-secret"; char *copy = strdup(token); char *p1 = strchr(copy, '.'); if (!p1) { free(copy); return -1; } *p1 = '\0'; char *h_enc = copy; char *rest = p1 + 1; char *p2 = strchr(rest, '.'); if (!p2) { free(copy); return -1; } *p2 = '\0'; char *p_enc = rest; char *s_enc = p2 + 1; size_t msglen = strlen(h_enc) + 1 + strlen(p_enc); char *msg = malloc(msglen + 1); snprintf(msg, msglen+1, "%s.%s", h_enc, p_enc); unsigned char sig[32]; hmac_sha256((unsigned char*)JWT_SECRET, strlen(JWT_SECRET), (unsigned char*)msg, strlen(msg), sig); char *calc_sig = base64url_encode(sig, 32); if (!calc_sig) { free(copy); free(msg); return -1; } if (strcmp(calc_sig, s_enc) != 0) { free(calc_sig); free(copy); free(msg); return -1; } free(calc_sig); size_t payload_len; unsigned char *payload_bin = base64_decode_alloc(p_enc, &payload_len); if (!payload_bin) { free(copy); free(msg); return -1; } char *payload = malloc(payload_len + 1); memcpy(payload, payload_bin, payload_len); payload[payload_len] = '\0'; free(payload_bin); long long exp = json_get_int(payload, "exp", 0); time_t now = time(NULL); if (exp <= now) { free(payload); free(copy); free(msg); return -1; } char *userId = json_get_string(payload, "userId"); char *roles = json_get_roles_csv(payload); *out_userId = userId; *out_roles_csv = roles; free(payload); free(copy); free(msg); return (userId ? 0 : -1); }

int is_valid_email(const char *email) { if (!email || !*email) return 0; const char *at = strchr(email, '@'); if (!at || at == email) return 0; const char *dot = strchr(at, '.'); return dot != NULL; }
int is_valid_password(const char *password) { if (!password) return 0; int len = strlen(password); if (len < 8) return 0; int hasDigit=0, hasLower=0, hasUpper=0, hasSpecial=0; for (int i=0;i<len;i++) { if (isdigit((unsigned char)password[i])) hasDigit=1; else if (islower((unsigned char)password[i])) hasLower=1; else if (isupper((unsigned char)password[i])) hasUpper=1; else if (isspace((unsigned char)password[i])) return 0; else hasSpecial=1; } return hasDigit && hasLower && hasUpper && hasSpecial; }
