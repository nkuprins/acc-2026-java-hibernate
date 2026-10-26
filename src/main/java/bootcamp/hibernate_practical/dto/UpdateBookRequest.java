package bootcamp.hibernate_practical.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

@Getter
public class UpdateBookRequest {
    @NotBlank
    private String title;
    @NotBlank
    private String author;
    @NotBlank
    private String genre;
    // If the JSON has no year, this int stays 0, so @Positive also makes the year required
    @Positive
    private int publicationYear;
    // Boolean, not boolean, so a missing value is null and @NotNull rejects it instead of it becoming false
    @NotNull
    private Boolean available;
}
