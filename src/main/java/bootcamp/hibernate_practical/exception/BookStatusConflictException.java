package bootcamp.hibernate_practical.exception;

public class BookStatusConflictException extends RuntimeException {
    public BookStatusConflictException(String message) {
        super(message);
    }
}
