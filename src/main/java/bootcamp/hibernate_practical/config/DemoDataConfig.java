package bootcamp.hibernate_practical.config;

import bootcamp.hibernate_practical.entity.AppUser;
import bootcamp.hibernate_practical.entity.Book;
import bootcamp.hibernate_practical.entity.Role;
import bootcamp.hibernate_practical.repository.AppUserRepository;
import bootcamp.hibernate_practical.repository.BookRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DemoDataConfig {

    @Bean
    public CommandLineRunner createDemoUsers(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            appUserRepository.save(new AppUser("user", passwordEncoder.encode("user"), Role.USER));
            appUserRepository.save(new AppUser("admin", passwordEncoder.encode("admin"), Role.ADMIN));
        };
    }

    @Bean
    public CommandLineRunner createDemoBooks(BookRepository bookRepository) {
        return args -> bookRepository.saveAll(List.of(
                new Book("Emma", "Jane Austen", "Novel", 1815, true),
                new Book("Dune", "Frank Herbert", "Science Fiction", 1965, true),
                new Book("The Hobbit", "J. R. R. Tolkien", "Fantasy", 1937, false)));
    }
}
