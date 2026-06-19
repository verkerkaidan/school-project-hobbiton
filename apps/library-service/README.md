# Library Microservice — Rust (Axum)

REST microservice aggregating **Open Library** (books), **OMDB** (movies), and **ExerciseDB** (exercises) with **Redis caching** and **Postgres-backed user lists**. Built with **Axum 0.8**, using **Strategy + Factory + Adapter** design patterns.

---

## Architecture

```
src/
├── main.rs                   Axum server, bootstraps AppState
├── lib.rs                    Library crate (imported by tests)
├── routes/mod.rs             Axum handlers — search, lists, items
├── service/mod.rs            LibraryService — cache-aside orchestration
├── cache/mod.rs              CacheService — Redis wrapper, key builders, auto-disable
├── db.rs                     DbService — Postgres CRUD for lists/list_items
├── models/                   Internal domain models (API-agnostic)
│   ├── book.rs / movie.rs / exercise.rs
├── dto/
│   ├── request.rs            SearchRequest, SearchType enum, query params
│   └── response.rs           ApiResponse<T>, SearchResult enum, *Response structs
├── strategy/                 ── STRATEGY PATTERN ──
│   ├── mod.rs                SearchStrategy trait
│   ├── book_strategy.rs      OpenLibraryStrategy
│   ├── movie_strategy.rs     OmdbStrategy
│   └── exercise_strategy.rs  ExerciseDBStrategy
├── adapters/                 ── ADAPTER PATTERN ──
│   ├── book_adapter.rs       Raw OpenLibrary JSON → BookResponse
│   ├── movie_adapter.rs      Raw OMDB JSON → MovieResponse
│   └── exercise_adapter.rs   Raw ExerciseDB JSON → ExerciseResponse
├── factory/mod.rs            ── FACTORY PATTERN ── HashMap<SearchType, Strategy>
└── error/mod.rs              AppError → HTTP response with IntoResponse
```

---

## Design Patterns

| Pattern | Role |
|---------|------|
| **Strategy** | Each external API implements `SearchStrategy` with `search()` and `find_by_id()` |
| **Factory** | `ServiceFactory` registers strategies in a `HashMap` — controllers ask by type |
| **Adapter** | Raw JSON → domain model → response DTO; no raw JSON escapes `adapters/` |

---

## Cache Flow (cache-aside)

```
  Request
    │
    ▼
┌──────────────┐   HIT   ┌────────────────────────┐
│    Redis     │────────▶│  Return cached result  │
│  CacheService│         └────────────────────────┘
└──────────────┘
    │ MISS
    ▼
┌──────────────┐
│  Strategy    │  calls external API
└──────────────┘
    │
    ▼
┌──────────────┐
│  Store in    │  TTL = 30 days
│  Redis       │
└──────────────┘
    │
    ▼
  Response
```

**Cache keys:**
| Endpoint | Key format | Example |
|----------|-----------|---------|
| Search | `search:{TYPE}:{query}:{max}` | `search:BOOK:clean code:10` |
| Find by ID | `item:{TYPE}:{id}` | `item:MOVIE:tt0111161` |

**Cache failures are non-fatal** — a Redis outage logs a warning and disables caching for 30 seconds, then re-enables. The service continues serving from live APIs.

---

## External APIs

| SearchType | API | Auth |
|------------|-----|------|
| `BOOK` | [Open Library](https://openlibrary.org) | None |
| `MOVIE` | [OMDB](https://www.omdbapi.com) | `OMDB_API_KEY` (free) |
| `EXERCISE` | [ExerciseDB](https://oss.exercisedb.dev) | None |

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DATABASE_URL` | — | Postgres connection string |
| `REDIS_URL` | `redis://127.0.0.1:6379` | Redis connection URL |
| `OMDB_API_KEY` | — | OMDB API key (required for movies) |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:23738` | Comma-separated CORS origins |

---

## Database

Postgres with two tables managed by sqlx migrations (`migrations/001_lists.sql`):

**`lists`** — `id`, `user_id`, `name`, `description`, `created_at`, `updated_at`

**`list_items`** — `id`, `list_id` (FK CASCADE), `item_type`, `item_id`, `position`, `notes`, `added_at` with `UNIQUE (list_id, item_id, item_type)`

---

## Setup

### Local (requires Redis + Postgres)

```bash
cd apps/library-service
cargo run
```

The service retries Redis every 2s and Postgres every 5s until they respond.

### Docker Compose

```bash
docker compose up --build
```

Redis data is persisted in a named Docker volume (`redis_data`).

### Tests

```bash
cd apps/library-service
cargo test
```

Unit tests are co-located with modules. Integration tests live in `tests/`:
cache, factory, error mapping, search strategies, routes, health, adapters.

---

## Endpoints (port 28877)

### POST /api/library/search
```bash
curl -X POST http://localhost:28877/api/library/search \
  -H "Content-Type: application/json" \
  -d '{"type": "BOOK", "query": "clean code", "maxResults": 5}'
```

### GET /api/library/search
```bash
curl "http://localhost:28877/api/library/search?query=batman&type=MOVIE&maxResults=5"
```

### GET /api/library/{type}/{id}
```bash
curl http://localhost:28877/api/library/BOOK/9780132350884   # ISBN
curl http://localhost:28877/api/library/MOVIE/tt0111161      # IMDb ID
curl http://localhost:28877/api/library/EXERCISE/42          # exercise ID
```

### GET /api/library/health
```bash
curl http://localhost:28877/api/library/health
```

### List CRUD

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/library/users/{uid}/lists` | Get all lists for user |
| `POST` | `/api/library/users/{uid}/lists` | Create list |
| `GET` | `/api/library/users/{uid}/lists/{lid}` | Get single list |
| `PUT` | `/api/library/users/{uid}/lists/{lid}` | Rename / update list |
| `DELETE` | `/api/library/users/{uid}/lists/{lid}` | Delete list |
| `GET` | `/api/library/lists/{lid}/items` | Get item IDs |
| `GET` | `/api/library/lists/{lid}/items/resolved` | Get items with full API data |
| `POST` | `/api/library/lists/{lid}/items` | Add item (verified against API) |
| `DELETE` | `/api/library/lists/{lid}/items/{iid}` | Remove item |
| `PUT` | `/api/library/lists/{lid}/items/{iid}` | Update item notes |
| `POST` | `/api/library/lists/{lid}/items/reorder` | Reorder items |

---

## Response Format

```json
{
  "success": true,
  "message": "Search completed successfully",
  "totalResults": 3,
  "timestamp": "2024-11-01T12:00:00Z",
  "data": [
    {
      "resultType": "book",
      "id": "9780132350884",
      "title": "Clean Code",
      "authors": ["Robert C. Martin"],
      "publishedDate": "2008",
      "publishers": ["Prentice Hall"],
      "isbn": ["9780132350884"],
      "pageCount": 464,
      "languages": ["eng"],
      "thumbnailUrl": "https://covers.openlibrary.org/b/id/12345-M.jpg",
      "coverId": "12345"
    }
  ]
}
```

---

## Inspecting the Cache (Redis CLI)

```bash
docker exec -it bpset-p4-redis-1 redis-cli
KEYS *
TTL "item:MOVIE:tt0111161"
GET "item:MOVIE:tt0111161"
DEL "search:BOOK:clean code:10"
```

---

## Adding a New API

1. Add a variant to `SearchType` in `src/dto/request.rs`
2. Create a domain model in `src/models/`
3. Create an adapter in `src/adapters/` (raw JSON → model → DTO)
4. Implement `SearchStrategy` in `src/strategy/`
5. Register in `ServiceFactory::new()` in `src/factory/mod.rs`

No changes needed to the cache layer, service, routes, or response envelope.
