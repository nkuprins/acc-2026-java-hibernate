package bootcamp.hibernate_practical.dto;

public record ExternalBookResponse(Long id,
                                   String title,
                                   String author,
                                   String genre,
                                   Double price,
                                   String isbn,
                                   Integer publishedYear) {
}
