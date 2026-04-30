package com.bookos.backend.source.repository;

import com.bookos.backend.source.entity.SourceReference;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceReferenceRepository extends JpaRepository<SourceReference, Long> {

    long countByUserId(Long userId);

    @EntityGraph(attributePaths = {"book", "note", "noteBlock"})
    Optional<SourceReference> findByIdAndUserId(Long id, Long userId);

    @EntityGraph(attributePaths = {"book", "note", "noteBlock"})
    List<SourceReference> findByUserIdOrderByCreatedAtDesc(Long userId);

    @EntityGraph(attributePaths = {"book", "note", "noteBlock"})
    List<SourceReference> findByBookIdAndUserIdOrderByCreatedAtDesc(Long bookId, Long userId);

    @EntityGraph(attributePaths = {"book", "note", "noteBlock"})
    List<SourceReference> findByNoteIdAndUserIdOrderByCreatedAtDesc(Long noteId, Long userId);

    @EntityGraph(attributePaths = {"book", "note", "noteBlock"})
    List<SourceReference> findByNoteBlockIdAndUserIdOrderByCreatedAtDesc(Long noteBlockId, Long userId);

    @EntityGraph(attributePaths = {"book", "note", "noteBlock"})
    List<SourceReference> findByRawCaptureIdAndUserIdOrderByCreatedAtDesc(Long rawCaptureId, Long userId);

    List<SourceReference> findByNoteBlockIdOrderByCreatedAtAsc(Long noteBlockId);

    void deleteByNoteBlockId(Long noteBlockId);

    List<SourceReference> findByRawCaptureIdOrderByCreatedAtAsc(Long rawCaptureId);

    void deleteByRawCaptureId(Long rawCaptureId);
}
