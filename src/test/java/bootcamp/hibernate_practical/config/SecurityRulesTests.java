package bootcamp.hibernate_practical.config;

import bootcamp.hibernate_practical.client.ExternalBookClient;
import bootcamp.hibernate_practical.exception.ExternalServiceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Own database: the demo users would otherwise be inserted twice into the in-memory DB
// shared with the other test context
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
class SecurityRulesTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExternalBookClient externalBookClient;

    @Test
    void rejectsRestRequestWithoutCredentials() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void redirectsBrowserWithoutLoginToLoginPage() throws Exception {
        mockMvc.perform(get("/books").accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void rejectsWrongPassword() throws Exception {
        mockMvc.perform(get("/api/books").with(httpBasic("user", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanReadBooks() throws Exception {
        mockMvc.perform(get("/api/books").with(httpBasic("user", "user")))
                .andExpect(status().isOk());
    }

    @Test
    void userCannotCreateBook() throws Exception {
        mockMvc.perform(postBookJson().with(httpBasic("user", "user")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanCreateBook() throws Exception {
        mockMvc.perform(postBookJson().with(httpBasic("admin", "admin")))
                .andExpect(status().isCreated());
    }

    @Test
    void booksPageHidesAddFormFromUser() throws Exception {
        mockMvc.perform(get("/books").with(httpBasic("user", "user")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Add a book"))));
    }

    @Test
    void booksPageShowsAddFormToAdmin() throws Exception {
        mockMvc.perform(get("/books").with(httpBasic("admin", "admin")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Add a book")));
    }

    @Test
    void rejectsFormPostWithoutCsrfToken() throws Exception {
        mockMvc.perform(postBookForm().with(httpBasic("admin", "admin")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanAddBookThroughFormWithCsrfToken() throws Exception {
        mockMvc.perform(postBookForm().with(httpBasic("admin", "admin")).with(csrf()))
                .andExpect(redirectedUrl("/books"));
    }

    @Test
    void userCannotAddBookThroughFormEvenWithCsrfToken() throws Exception {
        mockMvc.perform(postBookForm().with(httpBasic("user", "user")).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void returnsBadGatewayWhenExternalServiceFails() throws Exception {
        String failure = "External service returned 404 NOT_FOUND for book 997";
        given(externalBookClient.getBook(997L)).willThrow(new ExternalServiceException(failure));

        mockMvc.perform(get("/api/external-books/997").with(httpBasic("user", "user")))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.detail").value(failure));
    }

    private static MockHttpServletRequestBuilder postBookJson() {
        return post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"title": "Emma", "author": "Jane Austen", "genre": "Novel", "publicationYear": 1815}
                        """);
    }

    private static MockHttpServletRequestBuilder postBookForm() {
        return post("/books")
                .param("title", "Emma")
                .param("author", "Jane Austen")
                .param("genre", "Novel")
                .param("publicationYear", "1815");
    }
}
