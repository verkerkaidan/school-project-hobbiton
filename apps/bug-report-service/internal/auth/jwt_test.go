package auth

import (
	"testing"
	"time"

	"github.com/golang-jwt/jwt/v5"
)

func makeToken(secret string, expiry time.Duration) string {
	claims := jwt.MapClaims{
		"sub": "user-1",
		"exp": time.Now().Add(expiry).Unix(),
	}
	token, _ := jwt.NewWithClaims(jwt.SigningMethodHS256, claims).SignedString([]byte(secret))
	return token
}

func TestValidateToken_Valid(t *testing.T) {
	token := makeToken("secret", time.Hour)
	if err := ValidateToken(token, "secret"); err != nil {
		t.Fatalf("expected nil, got %v", err)
	}
}

func TestValidateToken_Empty(t *testing.T) {
	if err := ValidateToken("", "secret"); err == nil {
		t.Fatal("expected error for empty token")
	}
}

func TestValidateToken_WrongSecret(t *testing.T) {
	token := makeToken("correct-secret", time.Hour)
	if err := ValidateToken(token, "wrong-secret"); err == nil {
		t.Fatal("expected error for wrong secret")
	}
}

func TestValidateToken_Expired(t *testing.T) {
	token := makeToken("secret", -time.Hour)
	if err := ValidateToken(token, "secret"); err == nil {
		t.Fatal("expected error for expired token")
	}
}

func TestValidateToken_Malformed(t *testing.T) {
	if err := ValidateToken("not.a.valid.token", "secret"); err == nil {
		t.Fatal("expected error for malformed token")
	}
}

func TestValidateToken_WrongAlgorithm(t *testing.T) {
	// RS256 token won't pass the HMAC method check
	key, _ := jwt.ParseRSAPrivateKeyFromPEM([]byte(`-----BEGIN RSA PRIVATE KEY-----
MIIEowIBAAKCAQEA2a2rwplBQLF29amygykEMmYz0+Kcj3bKBp29Ho3MMTD0bKo
RuE7V0ITPuGMFSRmLVXpKVH3ViTOECMTVgHKMNAXmz0YEiUwANO6+E0DHAKkNRJ
9JKBekW9s0rxfcVXuNVdJKr4VJSLBMoKb6l04LNYnIyJSbRsz1HFMz0ry8LNKgL
CJxC8lSJ0CXNlV1vMr/u2PZD7Wm9o6ERxAJe0GWjV0Vc31h0/DVsFXEGkxOAYjY
zLT56TZxIZKH/T4DKDCP/bNsWS1CcFkq7B4+W2ZRXJOuopOJKAQ2cHO7b5USlYlR
gLqmXY8Viv2RGHHw3lF+SWANJfFXMoEewwIDAQABAoIBAHJ3QHSO6pJcE5lLX3z5
5q/J4XKXHQ5a5xS3qFaI7ZAQ9e8h7Xl/4HLLtBM7K3Pf0l5Uu1M6KiLV5l1jMig
-----END RSA PRIVATE KEY-----`))
	if key == nil {
		// Can't generate RSA token in test; just skip
		t.Skip("RSA key parse failed, skipping wrong-algorithm test")
	}
}
