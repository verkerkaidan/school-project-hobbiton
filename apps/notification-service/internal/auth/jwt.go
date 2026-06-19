package auth

import (
	"errors"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

// Claims holds the validated JWT payload.
type Claims struct {
	UserID uuid.UUID
	// Roles contains the values from the "role" array claim, e.g. ["ROLE_ADMIN"].
	Roles []string
}

// HasRole reports whether the claims contain the given role string.
func (c *Claims) HasRole(role string) bool {
	for _, r := range c.Roles {
		if r == role {
			return true
		}
	}
	return false
}

// ValidateToken validates an HMAC256-signed JWT and returns the userId and role
// claims. The "role" claim is a JSON array produced by the authentication-service
// (e.g. ["ROLE_ADMIN", "ROLE_USER"]). Missing or non-array role claims are
// treated as an empty role list rather than an error.
func ValidateToken(tokenString, secret string) (*Claims, error) {
	if tokenString == "" {
		return nil, errors.New("token is empty")
	}

	token, err := jwt.Parse(tokenString, func(token *jwt.Token) (interface{}, error) {
		if _, ok := token.Method.(*jwt.SigningMethodHMAC); !ok {
			return nil, errors.New("unexpected signing method")
		}
		return []byte(secret), nil
	})
	if err != nil {
		return nil, err
	}

	mapClaims, ok := token.Claims.(jwt.MapClaims)
	if !ok || !token.Valid {
		return nil, errors.New("invalid token claims")
	}

	userIDStr, ok := mapClaims["userId"].(string)
	if !ok || userIDStr == "" {
		return nil, errors.New("userId claim missing or not a string")
	}

	userID, err := uuid.Parse(userIDStr)
	if err != nil {
		return nil, errors.New("userId claim is not a valid UUID")
	}

	var roles []string
	if raw, ok := mapClaims["role"].([]interface{}); ok {
		for _, v := range raw {
			if s, ok := v.(string); ok {
				roles = append(roles, s)
			}
		}
	}

	return &Claims{UserID: userID, Roles: roles}, nil
}
