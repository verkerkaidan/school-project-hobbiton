// src/lib.rs
//
// This file is the library target — the only thing integration tests
// in tests/ can import from. main.rs is a binary target and is invisible
// to external crates.
//
// Rule: everything a test might need must be `pub` here.

pub mod adapters;
pub mod auth;
pub mod cache;
pub mod dto;
pub mod error;
pub mod factory;
pub mod metrics;
pub mod models;
pub mod routes;
pub mod service;
pub mod strategy;
pub mod db;

use std::sync::Arc;

use crate::cache::CacheService;
use crate::db::DbService;
use crate::factory::ServiceFactory;

/// Shared state injected into every Axum handler via `State<AppState>`.
///
/// Defined here (not in main.rs) so that both the binary *and* integration
/// tests can construct and use it.
#[derive(Clone)]
pub struct AppState {
    pub factory: Arc<ServiceFactory>,
    pub cache: Arc<CacheService>,
    pub db: Arc<DbService>,
}