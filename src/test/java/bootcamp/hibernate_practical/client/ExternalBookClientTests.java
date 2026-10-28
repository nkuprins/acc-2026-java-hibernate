package bootcamp.hibernate_practical.client;

import bootcamp.hibernate_practical.config.ExternalServiceProperties;
import bootcamp.hibernate_practical.dto.ExternalBookResponse;
import bootcamp.hibernate_practical.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ExternalBookClientTests {

    private static final String BOOK_URL = "http://external.test/api/books/1";

    private MockRestServiceServer server;
    private ExternalBookClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ExternalBookClient(builder, new ExternalServiceProperties("http://external.test/api", "test-token"));
    }

    @Test
    void sendsBearerTokenAndReturnsBook() {
        server.expect(requestTo(BOOK_URL))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andRespond(withSuccess("""
                        {"id": 1, "title": "The Spaghetti Whisperer", "author": "Sir Reginald Fluffington III",
                         "genre": "Software Engineering", "price": 34.99, "isbn": "978-0132350884", "publishedYear": 2008}
                        """, MediaType.APPLICATION_JSON));

        ExternalBookResponse book = client.getBook(1L);

        assertThat(book.title()).isEqualTo("The Spaghetti Whisperer");
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {401, 403, 404, 500, 503})
    void turnsErrorStatusIntoExternalServiceException(int status) {
        server.expect(requestTo(BOOK_URL)).andRespond(withStatus(HttpStatus.valueOf(status)));

        assertThatThrownBy(() -> client.getBook(1L))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining(String.valueOf(status));
    }

    @Test
    void turnsEmptyBodyIntoExternalServiceException() {
        server.expect(requestTo(BOOK_URL)).andRespond(withSuccess());

        assertThatThrownBy(() -> client.getBook(1L))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("empty body");
    }

    @Test
    void turnsMalformedJsonIntoExternalServiceException() {
        server.expect(requestTo(BOOK_URL)).andRespond(withSuccess("{\"id\": 1,,", MediaType.APPLICATION_JSON));

        assertThatThrownBy(() -> client.getBook(1L))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Unreadable response");
    }

    @Test
    void turnsTimeoutIntoExternalServiceException() {
        server.expect(requestTo(BOOK_URL)).andRespond(withException(new SocketTimeoutException("Read timed out")));

        assertThatThrownBy(() -> client.getBook(1L))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("timed out");
    }
}
