package com.example.bookservice.service;

import com.example.bookservice.exception.ResourceNotFoundException;
import com.example.bookservice.model.Book;
import com.example.bookservice.repository.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BookService {

    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> getAll() {
        return repository.findAll();
    }

    public Book getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id " + id));
    }

    public Book create(Book book) {
        book.setId(null);
        return repository.save(book);
    }

    @Transactional
    public Book update(Long id, Book input) {
        Book existing = getById(id);
        existing.setTitle(input.getTitle());
        existing.setAuthor(input.getAuthor());
        existing.setIsbn(input.getIsbn());
        existing.setPrice(input.getPrice());
        existing.setStock(input.getStock());
        return repository.save(existing);
    }

    public void delete(Long id) {
        Book existing = getById(id);
        repository.delete(existing);
    }
}
