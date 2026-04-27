package com.bookos.backend.note.repository;

import com.bookos.backend.note.entity.BookNote;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookNoteRepository extends JpaRepository<BookNote, Long> {

    @EntityGraph(attributePaths = {"book", "blocks"})
    List<BookNote> findByBookIdAndUserIdAndArchivedFalseOrderByUpdatedAtDesc(Long bookId, Long userId);

    @EntityGraph(attributePaths = {"book", "blocks"})
    Optional<BookNote> findByIdAndUserId(Long id, Long userId);
}
