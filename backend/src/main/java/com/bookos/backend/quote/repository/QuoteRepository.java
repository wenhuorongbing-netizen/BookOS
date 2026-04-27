package com.bookos.backend.quote.repository;

import com.bookos.backend.quote.entity.Quote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteRepository extends JpaRepository<Quote, Long> {

    @EntityGraph(attributePaths = {"book", "note", "noteBlock"})
    List<Quote> findByUserIdAndArchivedFalseOrderByUpdatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"book", "note", "noteBlock"})
    List<Quote> findByUserIdAndBookIdAndArchivedFalseOrderByUpdatedAtDesc(Long userId, Long bookId);

    @EntityGraph(attributePaths = {"book", "note", "noteBlock"})
    Optional<Quote> findByIdAndUserIdAndArchivedFalse(Long id, Long userId);
}
