package com.bookos.backend.note.repository;

import com.bookos.backend.note.entity.NoteBlock;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoteBlockRepository extends JpaRepository<NoteBlock, Long> {

    @EntityGraph(attributePaths = {"note", "book", "sourceReferences"})
    Optional<NoteBlock> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"sourceReferences"})
    List<NoteBlock> findByNoteIdAndUserIdOrderBySortOrderAsc(Long noteId, Long userId);
}
