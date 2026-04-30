package com.bookos.backend.book.repository;

import com.bookos.backend.book.entity.BookAuthor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BookAuthorRepository extends JpaRepository<BookAuthor, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from BookAuthor bookAuthor where bookAuthor.book.id = :bookId")
    void deleteAllByBookId(Long bookId);
}
