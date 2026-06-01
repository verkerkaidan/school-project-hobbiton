# Library Microservice — Rust (Axum)

REST microservice aggregating three external APIs with **Redis caching**.  
Built with **Axum** (not Rocket), using **Strategy + Factory + Adapter** design patterns.

---

## Architecture

```
src/
├── main.rs                   Axum server, AppState (factory + cache)
├── cache/mod.rs              CacheService — Redis wrapper, key builders, TTL
├── models/                   Internal domain models (API-agnostic)
│   ├── book.rs / movie.rs / exercise.rs
├── dto/
│   ├── request.rs            SearchRequest, SearchType, query params
│   └── response.rs           ApiResponse<T>, SearchResult enum, *Response structs
├── strategy/                 ── STRATEGY PATTERN ──
│   ├── mod.rs                SearchStrategy trait
│   ├── book_strategy.rs      OpenLibraryStrategy
│   ├── movie_strategy.rs     OmdbStrategy
│   └── exercise_strategy.rs  WgerStrategy
├── adapters/                 ── ADAPTER PATTERN ──
│   ├── book_adapter.rs       Raw Open Library JSON → BookResponse
│   ├── movie_adapter.rs      Raw OMDB JSON → MovieResponse
│   └── exercise_adapter.rs   Raw wger.de JSON → ExerciseResponse
├── factory/mod.rs            ── FACTORY PATTERN ── HashMap<SearchType, Strategy>
├── service/mod.rs            LibraryService — cache-aside orchestration
├── routes/mod.rs             Axum handlers
└── error/mod.rs              AppError → HTTP response
```

---

## Cache flow (cache-aside pattern)

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
│  Strategy    │  calls external API (OpenLibrary / OMDB / wger)
└──────────────┘
    │
    ▼
┌──────────────┐
│  Store in    │  TTL = 30 days (media data rarely changes)
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

**Cache failures are non-fatal** — a Redis outage causes a log warning and falls through to the live API, so the service keeps working.

---

## External APIs

| SearchType | API | Auth |
|------------|-----|------|
| `BOOK` | [Open Library](https://openlibrary.org) | None |
| `MOVIE` | [OMDB](https://www.omdbapi.com) | `OMDB_API_KEY` (free) |
| `EXERCISE` | [wger.de](https://wger.de/api/v2/) | None |

---

## Setup

### Local (requires Redis running)

```bash

cargo run
```

### Docker Compose (recommended — starts Redis + app together)

```bash

docker compose up --build
```

Redis data is persisted in a named Docker volume (`redis_data`).

---

## Endpoints

### POST /api/library/search

```bash
curl -X POST http://localhost:8080/api/library/search \
  -H "Content-Type: application/json" \
  -d '{"type": "BOOK", "query": "clean code", "maxResults": 5}'

curl -X POST http://localhost:8080/api/library/search \
  -H "Content-Type: application/json" \
  -d '{"type": "MOVIE", "query": "inception", "maxResults": 3}'

curl -X POST http://localhost:8080/api/library/search \
  -H "Content-Type: application/json" \
  -d '{"type": "EXERCISE", "query": "squat", "maxResults": 5}'
```

### GET /api/library/search

```bash
curl "http://localhost:8080/api/library/search?query=batman&type=MOVIE&maxResults=5"
curl "http://localhost:8080/api/library/search?query=bench+press&type=EXERCISE"
```

### GET /api/library/{type}/{id}

```bash
curl http://localhost:8080/api/library/BOOK/9780132350884   # ISBN
curl http://localhost:8080/api/library/MOVIE/tt0111161      # IMDb ID
curl http://localhost:8080/api/library/EXERCISE/42          # wger ID
```

### GET /api/library/health

```bash
curl http://localhost:8080/api/library/health
```

---

## Response format

```json
{
  "success": true,
  "message": "Search completed successfully",
  "totalResults": 3,
  "timestamp": "2024-11-01T12:00:00Z",
  "data": [
    {
      "resultType": "book",
      "title": "Clean Code",
      "authors": ["Robert C. Martin"],
      "...": "..."
    }
  ]
}
```

---

## Inspecting the cache (Redis CLI)

```bash
# Connect
docker exec -it library_redis redis-cli

# List all cache keys
KEYS *

# Check TTL (seconds remaining) on a key
TTL "item:MOVIE:tt0111161"

# Read a cached value
GET "item:MOVIE:tt0111161"

# Manually invalidate a key
DEL "search:BOOK:clean code:10"
```

---

## Adding a new API

1. Add a variant to `SearchType` in `dto/request.rs`
2. Create a domain model in `models/`
3. Create an adapter in `adapters/`
4. Implement `SearchStrategy` in `strategy/`
5. Register in `ServiceFactory::new()` in `factory/mod.rs`

No changes needed to the cache layer, service, routes, or response envelope.
