package com.bookos.backend.book.repository;

import com.bookos.backend.book.entity.UserBook;
import com.bookos.backend.common.enums.ReadingStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBookRepository extends JpaRepository<UserBook, Long> {

    @EntityGraph(attributePaths = {"book", "book.bookAuthors", "book.bookAuthors.author", "book.bookTags", "book.bookTags.tag"})
    List<UserBook> findByUserIdOrderByUpdatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"book", "book.bookAuthors", "book.bookAuthors.author", "book.bookTags", "book.bookTags.tag"})
    Optional<UserBook> findByIdAndUserId(Long id, Long userId);

    Optional<UserBook> findByUserIdAndBookId(Long userId, Long bookId);

    @EntityGraph(attributePaths = {"book", "book.bookAuthors", "book.bookAuthors.author", "book.bookTags", "book.bookTags.tag"})
    List<UserBook> findByUserIdAndReadingStatusInOrderByUpdatedAtDesc(Long userId, Collection<ReadingStatus> statuses);

    @EntityGraph(attributePaths = {"book", "book.bookAuthors", "book.bookAuthors.author", "book.bookTags", "book.bookTags.tag"})
    List<UserBook> findByUserIdAndRatingOrderByUpdatedAtDesc(Long userId, Integer rating);

    void deleteAllByBookId(Long bookId);
}
