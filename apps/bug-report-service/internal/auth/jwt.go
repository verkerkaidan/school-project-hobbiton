package auth

import (
	"errors"

	"github.com/golang-jwt/jwt/v5"
)

// ValidateToken validates an HMAC256-signed JWT and returns nil on success.
// We only validate presence of a valid token — reports are anonymous so no
// user identity is extracted or stored.
func ValidateToken(tokenString, secret string) error {
	if tokenString == "" {
		return errors.New("token is empty")
	}

	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("unexpected signing method")
		}
		return []byte(secret), nil
	})
	if err != nil {
		return err
	}

	if !token.Valid {
		return errors.New("invalid token")
	}

	return nil
}
