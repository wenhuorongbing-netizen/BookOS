package com.bookos.backend.source.repository;

import com.bookos.backend.source.entity.SourceReference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SourceReferenceRepository extends JpaRepository<SourceReference, Long> {

    List<SourceReference> findByNoteBlockIdOrderByCreatedAtAsc(Long noteBlockId);

    void deleteByNoteBlockId(Long noteBlockId);

    List<SourceReference> findByRawCaptureIdOrderByCreatedAtAsc(Long rawCaptureId);

    void deleteByRawCaptureId(Long rawCaptureId);
}
