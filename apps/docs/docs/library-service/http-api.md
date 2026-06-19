---
sidebar_position: 3
---

# HTTP API

All endpoints are served under the `/api/library` prefix via the API Gateway.

## Service Endpoints

| Method | Path | Auth | Description |
| --- | --- | --- | --- |
| `GET` | `/health` | none | Liveness check. |
| `POST` | `/search` | none | Search for books, movies, or exercises. |
| `GET` | `/search` | none | Search via query parameters. |
| `GET` | `/{type}/{id}` | none | Fetch a single item by ID. |
| `GET` | `/users/{user_id}/lists` | none | Get all lists for a user. |
| `POST` | `/users/{user_id}/lists` | none | Create a new list. |
| `GET` | `/users/{user_id}/lists/{list_id}` | none | Get a single list. |
| `PUT` | `/users/{user_id}/lists/{list_id}` | none | Rename or update a list. |
| `DELETE` | `/users/{user_id}/lists/{list_id}` | none | Delete a list. |
| `GET` | `/lists/{list_id}/items` | none | Get raw items (IDs only). |
| `GET` | `/lists/{list_id}/items/resolved` | none | Get items hydrated with full API data. |
| `POST` | `/lists/{list_id}/items` | none | Add an item to a list. |
| `DELETE` | `/lists/{list_id}/items/{item_id}` | none | Remove an item from a list. |
| `PUT` | `/lists/{list_id}/items/{item_id}` | none | Update notes on an item. |
| `POST` | `/lists/{list_id}/items/reorder` | none | Reorder items in a list. |

## Search Types

| Type | API | ID format |
| --- | --- | --- |
| `BOOK` | Open Library | ISBN (e.g. `9780132350884`) |
| `MOVIE` | OMDB | IMDb ID (e.g. `tt0111161`) |
| `EXERCISE` | ExerciseDB | Numeric ID (e.g. `42`) |

## Search

Search supports both POST and GET. The POST body and GET query parameters follow the same schema.

### POST `/api/library/search`

```json
{ "type": "BOOK", "query": "clean code", "maxResults": 10 }
```

| Field | Type | Default | Description |
| --- | --- | --- | --- |
| `type` | string | required | `BOOK`, `MOVIE`, or `EXERCISE` |
| `query` | string | required | Search query |
| `maxResults` | number | `10` | Maximum results to return |

### GET `/api/library/search`

```
GET /api/library/search?query=inception&type=MOVIE&maxResults=5
```

## Find by ID

```
GET /api/library/BOOK/9780132350884
GET /api/library/MOVIE/tt0111161
GET /api/library/EXERCISE/42
```

Returns a single result or 404.

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

### Book Response Fields

| Field | Type | Description |
| --- | --- | --- |
| `id` | string | ISBN or Open Library key |
| `title` | string | Book title |
| `authors` | string[] | Author names |
| `publishedDate` | string | First publish year |
| `publishers` | string[] | Publisher names |
| `isbn` | string[] | ISBN identifiers |
| `pageCount` | number | Page count (median across editions) |
| `languages` | string[] | Language codes (e.g. `eng`) |
| `thumbnailUrl` | string | Open Library cover URL |
| `coverId` | string | Open Library cover ID |

### Movie Response Fields

| Field | Type | Description |
| --- | --- | --- |
| `id` | string | IMDb ID (e.g. `tt0111161`) |
| `title` | string | Movie title |
| `year` | string | Release year |
| `rated` | string | MPAA rating (e.g. `R`, `PG-13`) |
| `released` | string | Release date |
| `runtime` | string | Duration (e.g. `142 min`) |
| `genre` | string | Genre(s) |
| `director` | string | Director |
| `writer` | string | Writer(s) |
| `actors` | string | Cast |
| `plot` | string | Synopsis |
| `language` | string | Language(s) |
| `country` | string | Country of origin |
| `awards` | string | Award nominations/wins |
| `poster` | string | Movie poster URL |
| `imdbRating` | string | IMDb rating (e.g. `9.3`) |
| `mediaType` | string | Type (e.g. `movie`) |

### Exercise Response Fields

| Field | Type | Description |
| --- | --- | --- |
| `exerciseId` | string | ExerciseDB ID |
| `name` | string | Exercise name |
| `gifUrl` | string | Demonstration GIF URL |
| `targetMuscles` | string[] | Primary muscles worked |
| `bodyParts` | string[] | Body parts involved |
| `equipments` | string[] | Required equipment |
| `secondaryMuscles` | string[] | Secondary muscles |
| `instructions` | string[] | Step-by-step instructions |

## Lists

All list endpoints follow RESTful conventions.

### Create a List

```
POST /api/library/users/{userId}/lists
```

```json
{ "name": "Favorites", "description": "My top picks" }
```

### Get User Lists

```
GET /api/library/users/{userId}/lists
```

### Get Single List

```
GET /api/library/users/{userId}/lists/{listId}
```

### Update List

```
PUT /api/library/users/{userId}/lists/{listId}
```

```json
{ "name": "New Name", "description": "Updated description" }
```

### Delete List

```
DELETE /api/library/users/{userId}/lists/{listId}
```

### Add Item

```
POST /api/library/lists/{listId}/items
```

```json
{ "itemType": "MOVIE", "itemId": "tt0111161", "notes": "Watch this!" }
```

The service verifies the item exists in the external API before adding it. Duplicates (same `list_id` + `item_type` + `item_id`) are silently ignored.

### Get Items (IDs only)

```
GET /api/library/lists/{listId}/items
```

### Get Resolved Items (Full Data)

```
GET /api/library/lists/{listId}/items/resolved
```

Hydrates each item with full details from the external API (cached after first fetch).

### Update Item Notes

```
PUT /api/library/lists/{listId}/items/{itemId}
```

```json
{ "notes": "Updated note" }
```

### Remove Item

```
DELETE /api/library/lists/{listId}/items/{itemId}
```

### Reorder Items

```
POST /api/library/lists/{listId}/items/reorder
```

```json
{ "itemIds": ["id-a", "id-b", "id-c"] }
```

Items are assigned position 0, 1, 2, … in the order provided. Items not in the list are ignored.

## Error Responses

Errors follow the same envelope with `success: false` and an appropriate HTTP status code.

| HTTP Status | Error Variant | Example |
| --- | --- | --- |
| `400` | `UnknownSearchType` | Unknown search type: `GAME` |
| `404` | `NotFound` | No result found for id: `invalid` |
| `502` | `ExternalApi` | OMDB API returned an error |
| `500` | `Cache` | Redis connection error |
| `500` | `Internal` | Database query failed |

```json
{
  "success": false,
  "message": "No service registered for search type: GAME",
  "totalResults": 0,
  "data": null,
  "timestamp": "2024-11-01T12:00:00Z"
}
```
