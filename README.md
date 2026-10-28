# Practical Task: Library Book Tracker API

Starter project for Data Architecture practical task: Build a REST API for managing books in a library using Spring Boot, Spring Data JPA and an H2 database.

## Task
Complete all of the 'TODO' portions of the code. The finished application should compile and all of the controller endpoints should be functional.

## Behavioural Requirements
- Books can be created
- All books can be retrieved
- A single book can be retrieved by ID
- Books can be deleted
- Books can be filtered and searched by their fields.
- Controller delegates to Service, which delegates to Repository
- DTOs are used for requests and responses

## Stretch goals
- Request validation
- Custom exception handling
- Searching by partial title
- Add 'borrow' and 'return' endpoints along with the new 'borrowedStatus' field to the Book entity

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