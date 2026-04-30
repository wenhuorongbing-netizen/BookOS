package com.bookos.backend.book.repository;

import com.bookos.backend.book.entity.BookTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BookTagRepository extends JpaRepository<BookTag, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from BookTag bookTag where bookTag.book.id = :bookId")
    void deleteAllByBookId(Long bookId);
}
