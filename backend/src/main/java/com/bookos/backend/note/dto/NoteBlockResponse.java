package com.bookos.backend.note.dto;

import com.bookos.backend.common.enums.NoteBlockType;
import com.bookos.backend.source.dto.SourceReferenceResponse;
import java.time.Instant;
import java.util.List;

public record NoteBlockResponse(
        Long id,
        Long noteId,
        Long bookId,
        NoteBlockType blockType,
        String rawText,
        String markdown,
        String plainText,
        Integer sortOrder,
        Integer pageStart,
        Integer pageEnd,
        List<String> parserWarnings,
        List<SourceReferenceResponse> sourceReferences,
        Instant createdAt,
        Instant updatedAt) {}
