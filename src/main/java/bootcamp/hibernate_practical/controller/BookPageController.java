package bootcamp.hibernate_practical.controller;

import bootcamp.hibernate_practical.dto.CreateBookRequest;
import bootcamp.hibernate_practical.service.BookService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/books")
public class BookPageController {
    private final BookService bookService;

    public BookPageController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String showBooks(Model model) {
        model.addAttribute("newBook", new CreateBookRequest());
        return booksPage(model);
    }

    @PostMapping
    public String addBook(@Valid @ModelAttribute("newBook") CreateBookRequest newBook, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            return booksPage(model);
        }
        bookService.createBook(newBook);
        return "redirect:/books";
    }

    private String booksPage(Model model) {
        model.addAttribute("books", bookService.getAllBooks());
        return "books";
    }
}
