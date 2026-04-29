package com.bookos.backend.book.repository;

import com.bookos.backend.book.entity.Book;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Override
    @EntityGraph(attributePaths = {"owner", "owner.role", "bookAuthors", "bookAuthors.author", "bookTags", "bookTags.tag"})
    List<Book> findAll();

    @EntityGraph(attributePaths = {"owner", "owner.role", "bookAuthors", "bookAuthors.author", "bookTags", "bookTags.tag"})
    List<Book> findAllByOrderByTitleAsc();

    @EntityGraph(attributePaths = {"owner", "owner.role", "bookAuthors", "bookAuthors.author", "bookTags", "bookTags.tag"})
    Optional<Book> findByTitleIgnoreCase(String title);

    @Override
    @EntityGraph(attributePaths = {"owner", "owner.role", "bookAuthors", "bookAuthors.author", "bookTags", "bookTags.tag"})
    Optional<Book> findById(Long id);
}
