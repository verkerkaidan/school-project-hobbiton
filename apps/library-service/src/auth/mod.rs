use axum::{
    body::Body,
    extract::Request,
    http::{header::AUTHORIZATION, StatusCode},
    middleware::Next,
    response::{IntoResponse, Response},
    Json,
};
use jsonwebtoken::{decode, DecodingKey, Validation, Algorithm};
use serde::{Deserialize, Serialize};
use serde_json::json;

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct JwtClaims {
    pub sub: String,
    #[serde(rename = "userId")]
    pub user_id: String,
    pub role: Vec<String>,
    pub iat: usize,
    pub exp: usize,
    pub iss: String,
}

pub async fn require_auth(mut request: Request<Body>, next: Next) -> Response {
    let path = request.uri().path().to_string();

    if path.ends_with("/health") || path.ends_with("/metrics") {
        return next.run(request).await;
    }

    let auth_header = match request.headers().get(AUTHORIZATION) {
        Some(v) => v.to_str().unwrap_or(""),
        None => return unauthorized("Missing Authorization header"),
    };

    let token = match auth_header.strip_prefix("Bearer ") {
        Some(t) => t,
        None => return unauthorized("Invalid Authorization header format"),
    };

    let secret = std::env::var("JWT_SECRET").unwrap_or_default();
    if secret.is_empty() {
        return unauthorized("JWT_SECRET not configured");
    }

    match decode::<JwtClaims>(
        token,
        &DecodingKey::from_secret(secret.as_bytes()),
        &Validation::new(Algorithm::HS256),
    ) {
        Ok(data) => {
            request.extensions_mut().insert(data.claims);
            next.run(request).await
        }
        Err(_) => unauthorized("Invalid or expired token"),
    }
}

fn unauthorized(message: &str) -> Response {
    let body = Json(json!({
        "success": false,
        "message": message,
        "totalResults": 0,
        "data": null,
        "timestamp": chrono::Utc::now().to_rfc3339()
    }));
    (StatusCode::UNAUTHORIZED, body).into_response()
}
