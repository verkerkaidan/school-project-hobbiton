package auth

import (
	"errors"

	"github.com/golang-jwt/jwt/v5"
	"github.com/google/uuid"
)

// Claims holds the validated JWT payload.
type Claims struct {
	UserID uuid.UUID
}

// ValidateToken validates an HMAC256-signed JWT and returns the userId claim.
// This mirrors the validation logic in the authentication-service (JWTUtil.java).
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

	return &Claims{UserID: userID}, nil
}
