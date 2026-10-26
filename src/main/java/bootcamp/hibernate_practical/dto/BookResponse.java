package bootcamp.hibernate_practical.dto;

import bootcamp.hibernate_practical.entity.BorrowedStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BookResponse {
    public Long id;
    public String title;
    public String author;
    public String genre;
    public int publicationYear;
    public boolean available;
    public BorrowedStatus borrowedStatus;
}
