package bootcamp.hibernate_practical.client;

import bootcamp.hibernate_practical.config.ExternalServiceProperties;
import bootcamp.hibernate_practical.dto.ExternalBookResponse;
import bootcamp.hibernate_practical.exception.ExternalServiceException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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
        try {
            ExternalBookResponse book = restClient.get()
                    .uri("/books/{id}", id)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, response) -> {
                        throw new ExternalServiceException("External service returned " + response.getStatusCode() + " for book " + id);
                    })
                    .body(ExternalBookResponse.class);
            if (book == null) {
                throw new ExternalServiceException("External service returned an empty body for book " + id);
            }
            return book;
        } catch (ResourceAccessException e) {
            throw new ExternalServiceException("External service unreachable or timed out for book " + id, e);
        } catch (RestClientException e) {
            throw new ExternalServiceException("Unreadable response from external service for book " + id, e);
        }
    }
}
