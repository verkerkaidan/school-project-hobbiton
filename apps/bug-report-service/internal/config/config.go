package config

import "os"

type Config struct {
	Port            string
	DBHost          string
	DBPort          string
	DBUser          string
	DBPassword      string
	DBName          string
	DBAdminUser     string
	DBAdminPassword string
	JWTSecret       string
	JWTIssuer       string
	AllowedOrigins  string
}

func Load() *Config {
	return &Config{
		Port:            getEnv("PORT", "29556"),
		DBHost:          getEnv("DB_HOST", "localhost"),
		DBPort:          getEnv("DB_PORT", "5432"),
		DBUser:          getEnv("DB_USER", ""),
		DBPassword:      getEnv("DB_PASSWORD", ""),
		DBName:          getEnv("DB_NAME", "bug_reports"),
		DBAdminUser:     getEnv("POSTGRES_USER", ""),
		DBAdminPassword: getEnv("POSTGRES_PASSWORD", ""),
		JWTSecret:       getEnv("JWT_SECRET", ""),
		JWTIssuer:       getEnv("JWT_ISSUER", ""),
		AllowedOrigins:  getEnv("BUG_REPORT_ALLOWED_ORIGINS", "*"),
	}
}

func getEnv(key, fallback string) string {
	if v := os.Getenv(key); v != "" {
		return v
	}
	return fallback
}
