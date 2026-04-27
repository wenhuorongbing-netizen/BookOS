package com.bookos.backend.book.repository;

import com.bookos.backend.book.entity.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookTagRepository extends JpaRepository<BookTag, Long> {}
