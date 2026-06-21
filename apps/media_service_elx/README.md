# MediaServiceElx
To start:
Run `docker compose up -d --build media-service-elx` in the terminal van Hobbithon.
Dan kan de service bereikt worden via `http://localhost:27086`.

## Endpoints
Endpoints can be found in `lib/media_service_elx_web/router.ex`
* Media related endpoints are found by 
  * `scope "/api/media"`
* Report related endpoints are found by 
  * `scope "/api/report"`
* User related endpoints are found by
  * `scope "/api/user"`

Most endpoints require a Bearer JWT. The unauthenticated service-to-service routes are `GET /api/media` and `PUT /api/media`.

## Documentation
Inline documentation is available. Alternatively, in the terminal (in `..\apps\media_service_elx`) 
you can run `mix docs`.
Then go to `doc/index.html` for a complete overview of documentation. 
This is a combination of my own and automatically generated documentation, so not everything will be useful.

## Testing
To run tests, in the terminal (in `..\apps\media_service_elx`)
you can run `mix test`. 
Before committing, run `mix precommit` to compile with warnings as errors, format, and run the test suite.
