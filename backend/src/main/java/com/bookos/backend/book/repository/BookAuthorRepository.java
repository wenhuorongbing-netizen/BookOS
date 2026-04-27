package com.bookos.backend.book.repository;

import com.bookos.backend.book.entity.BookAuthor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookAuthorRepository extends JpaRepository<BookAuthor, Long> {}
