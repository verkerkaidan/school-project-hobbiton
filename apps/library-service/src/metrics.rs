use axum::{
    body::Body,
    extract::Request,
    middleware::Next,
    response::{IntoResponse, Response},
};
use prometheus::{
    gather, register_histogram_vec, register_int_counter,
    register_int_counter_vec, Encoder, HistogramVec, IntCounter, IntCounterVec,
    TextEncoder,
};
use std::sync::LazyLock;

pub static HTTP_REQUESTS_TOTAL: LazyLock<IntCounterVec> = LazyLock::new(|| {
    register_int_counter_vec!(
        "library_http_requests_total",
        "Total number of HTTP requests",
        &["method", "path", "status"]
    )
    .unwrap()
});

pub static HTTP_REQUEST_DURATION_SECONDS: LazyLock<HistogramVec> = LazyLock::new(|| {
    register_histogram_vec!(
        "library_http_request_duration_seconds",
        "HTTP request latency in seconds",
        &["method", "path"],
        vec![0.005, 0.01, 0.025, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0]
    )
    .unwrap()
});

pub static CACHE_HITS_TOTAL: LazyLock<IntCounter> = LazyLock::new(|| {
    register_int_counter!("library_cache_hits_total", "Total Redis cache hits").unwrap()
});

pub static CACHE_MISSES_TOTAL: LazyLock<IntCounter> = LazyLock::new(|| {
    register_int_counter!("library_cache_misses_total", "Total Redis cache misses").unwrap()
});

pub static CACHE_ERRORS_TOTAL: LazyLock<IntCounter> = LazyLock::new(|| {
    register_int_counter!("library_cache_errors_total", "Total Redis cache errors").unwrap()
});

pub static EXTERNAL_API_REQUESTS_TOTAL: LazyLock<IntCounterVec> = LazyLock::new(|| {
    register_int_counter_vec!(
        "library_external_api_requests_total",
        "Total external API requests",
        &["api"]
    )
    .unwrap()
});

pub static EXTERNAL_API_DURATION_SECONDS: LazyLock<HistogramVec> = LazyLock::new(|| {
    register_histogram_vec!(
        "library_external_api_duration_seconds",
        "External API request latency in seconds",
        &["api"],
        vec![0.01, 0.05, 0.1, 0.25, 0.5, 1.0, 2.5, 5.0, 10.0, 30.0]
    )
    .unwrap()
});

pub static DB_REQUESTS_TOTAL: LazyLock<IntCounterVec> = LazyLock::new(|| {
    register_int_counter_vec!(
        "library_db_requests_total",
        "Total database queries",
        &["operation"]
    )
    .unwrap()
});

pub async fn track_metrics(request: Request<Body>, next: Next) -> Response {
    let path = request.uri().path().to_string();
    let method = request.method().to_string();

    if path.ends_with("/metrics") {
        return next.run(request).await;
    }

    let start = tokio::time::Instant::now();
    let response = next.run(request).await;
    let duration = start.elapsed().as_secs_f64();
    let status = response.status().as_str().to_string();

    HTTP_REQUESTS_TOTAL
        .with_label_values(&[&method, &path, &status])
        .inc();
    HTTP_REQUEST_DURATION_SECONDS
        .with_label_values(&[&method, &path])
        .observe(duration);

    response
}

pub async fn metrics_handler() -> impl IntoResponse {
    let encoder = TextEncoder::new();
    let metric_families = gather();
    let mut buffer = Vec::new();
    encoder.encode(&metric_families, &mut buffer).unwrap();
    (
        [(axum::http::header::CONTENT_TYPE, prometheus::TEXT_FORMAT)],
        buffer,
    )
}
