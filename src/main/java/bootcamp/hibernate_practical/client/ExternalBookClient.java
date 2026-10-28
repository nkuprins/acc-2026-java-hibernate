package bootcamp.hibernate_practical.client;

import bootcamp.hibernate_practical.config.ExternalServiceProperties;
import bootcamp.hibernate_practical.dto.ExternalBookResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ExternalBookClient {
    private final RestClient restClient;

    public ExternalBookClient(RestClient.Builder builder, ExternalServiceProperties properties) {
        this.restClient = builder
                .baseUrl(properties.baseUrl())
                .defaultHeader("Authorization", "Bearer " + properties.token())
                .build();
    }

    public ExternalBookResponse getBook(Long id) {
        return restClient.get()
                .uri("/books/{id}", id)
                .retrieve()
                .body(ExternalBookResponse.class);
    }
}
