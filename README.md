# Practical Task: Library Book Tracker API

> **Note for the reviewer:** `GET /api/external-books/{id}` calls the bootcamp book service and sends `Authorization: Bearer <token>`.
> The token comes from the `EXTERNAL_SERVICE_TOKEN` environment variable, not from the code.
> The bootcamp service does not check the token, so the call also works without it; the token is sent to show a secured outbound call.

Starter project for Data Architecture practical task: Build a REST API for managing books in a library using Spring Boot, Spring Data JPA and an H2 database.

## Security checks (Bruno screenshots)
Demo users: `user` / `user` (role USER) and `admin` / `admin` (role ADMIN).
Screenshots of the manual checks are in [`bruno-screenshots/`](bruno-screenshots/):

| Check | Request | Result | Screenshot |
|---|---|---|---|
| Unauthenticated request | `GET /api/books` without valid credentials | `401` | [bruno-401.png](bruno-screenshots/bruno-401.png) |
| USER reads data | `GET /api/books` as `user` | `200` | [bruno-reading-200.png](bruno-screenshots/bruno-reading-200.png) |
| USER writes data | `POST /api/books` as `user` | `403` | [bruno-user-write-403.png](bruno-screenshots/bruno-user-write-403.png) |
| ADMIN writes data | `POST /api/books` as `admin` | `201` | [bruno-admin-write-201.png](bruno-screenshots/bruno-admin-write-201.png) |
| Missing CSRF token | `POST /books` (browser form) as `admin`, without `_csrf` | `403` | [bruno-nocsrf-page-write-403(not-api).png](<bruno-screenshots/bruno-nocsrf-page-write-403(not-api).png>) |
| External API failure | `GET /api/external-books/999` (external service too slow) | `502` with a clear message | [bruno-external-handled.png](bruno-screenshots/bruno-external-handled.png) |

The same checks are automated in `SecurityRulesTests` and `ExternalBookClientTests` (`./mvnw test`).

## API notes

### Updating a book (`PUT /api/books/{id}`)
The request body must include every field, `available` and `version` among them:

```json
{
  "title": "Emma",
  "author": "Jane Austen",
  "genre": "Novel",
  "publicationYear": 1815,
  "available": true,
  "version": 0
}
```

`version` is the value from the latest `GET` of the book. It is used for optimistic locking:
- missing `version` -> `400 Bad Request`
- `version` older than the stored one (someone else updated the book in the meantime) -> `409 Conflict`

### Meaning of `available` and `borrowedStatus`
- `available` means the book is in the library's stock (not lost, withdrawn or in repair). It is set when a book is created or updated, not by borrowing.
- `borrowedStatus` says whether the book is currently on loan (`BORROWED` / `NOT_BORROWED`).

A book can be `available: true` and `BORROWED` at the same time: it belongs to the stock but is lent out right now. 
So `GET /api/books/available` lists all books in stock, borrowed ones included. 
A book can be borrowed only when it is `available` and `NOT_BORROWED`.