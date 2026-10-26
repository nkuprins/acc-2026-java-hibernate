package bootcamp.hibernate_practical.service;

import bootcamp.hibernate_practical.dto.BookResponse;
import bootcamp.hibernate_practical.dto.CreateBookRequest;
import bootcamp.hibernate_practical.dto.UpdateBookRequest;
import bootcamp.hibernate_practical.entity.Book;
import bootcamp.hibernate_practical.entity.BorrowedStatus;
import bootcamp.hibernate_practical.exception.BookNotFoundException;
import bootcamp.hibernate_practical.exception.BookStatusConflictException;
import bootcamp.hibernate_practical.repository.BookRepository;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {
    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public BookResponse createBook(CreateBookRequest request) {
        Book book = new Book(
                request.getTitle(),
                request.getAuthor(),
                request.getGenre(),
                request.getPublicationYear(),
                true
        );
        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    public List<BookResponse> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public BookResponse getBookById(Long id) {
        Book book = findBookOrThrow(id);
        return mapToResponse(book);
    }

    public BookResponse updateBook(Long id, UpdateBookRequest request) {
        Book book = findBookOrThrow(id);
        // Compared by hand because @Version only checks the version loaded in this request, not the client's
        if (!book.getVersion().equals(request.getVersion())) {
            throw new OptimisticLockingFailureException("Book " + id + " is at version " + book.getVersion() + ", request has " + request.getVersion());
        }
        if (!request.getAvailable() && book.getBorrowedStatus() == BorrowedStatus.BORROWED) {
            throw new BookStatusConflictException("Book " + id + " is borrowed and must be returned before it can be marked unavailable");
        }
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setGenre(request.getGenre());
        book.setPublicationYear(request.getPublicationYear());
        book.setAvailable(request.getAvailable());
        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    public void deleteBook(Long id) {
        // Loads the book instead of using existsById, so the borrowed check can read its status
        Book book = findBookOrThrow(id);
        if (book.getBorrowedStatus() == BorrowedStatus.BORROWED) {
            throw new BookStatusConflictException("Book " + id + " is borrowed and must be returned before it can be deleted");
        }
        bookRepository.delete(book);
    }

    public List<BookResponse> findByAuthor(String author) {
        return bookRepository.findByAuthor(author).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookResponse> findAvailableBooks(){
        return bookRepository.findByAvailableTrue().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookResponse> findPublishedAfter(int year) {
        return bookRepository.findByPublicationYearGreaterThan(year).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<BookResponse> searchByTitle(String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public long countBooks() {
        return bookRepository.count();
    }

    public BookResponse borrowBook(Long id) {
        Book book = findBookOrThrow(id);
        if (!book.isAvailable()) {
            throw new BookStatusConflictException("Book " + id + " is not in stock");
        }
        if (book.getBorrowedStatus() == BorrowedStatus.BORROWED) {
            throw new BookStatusConflictException("Book " + id + " is already borrowed");
        }
        book.setBorrowedStatus(BorrowedStatus.BORROWED);
        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    public BookResponse returnBook(Long id) {
        Book book = findBookOrThrow(id);
        if (book.getBorrowedStatus() != BorrowedStatus.BORROWED) {
            throw new BookStatusConflictException("Book " + id + " is not borrowed");
        }
        book.setBorrowedStatus(BorrowedStatus.NOT_BORROWED);
        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    private Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    private BookResponse mapToResponse(Book book) {
        return new BookResponse(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getGenre(),
                book.getPublicationYear(),
                book.isAvailable(),
                book.getBorrowedStatus(),
                book.getVersion()
        );
    }
}
