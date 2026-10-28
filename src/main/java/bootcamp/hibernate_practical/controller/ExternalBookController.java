package bootcamp.hibernate_practical.controller;

import bootcamp.hibernate_practical.client.ExternalBookClient;
import bootcamp.hibernate_practical.dto.ExternalBookResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/external-books")
public class ExternalBookController {
    private final ExternalBookClient externalBookClient;

    public ExternalBookController(ExternalBookClient externalBookClient) {
        this.externalBookClient = externalBookClient;
    }

    @GetMapping("/{id}")
    public ExternalBookResponse getExternalBook(@PathVariable Long id) {
        return externalBookClient.getBook(id);
    }
}
