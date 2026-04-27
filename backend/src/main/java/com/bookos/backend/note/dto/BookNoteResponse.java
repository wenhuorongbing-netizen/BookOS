package com.bookos.backend.note.dto;

import com.bookos.backend.common.enums.Visibility;
import java.time.Instant;
import java.util.List;

public record BookNoteResponse(
        Long id,
        Long bookId,
        String bookTitle,
        String title,
        String markdown,
        String threeSentenceSummary,
        Visibility visibility,
        boolean archived,
        List<NoteBlockResponse> blocks,
        Instant createdAt,
        Instant updatedAt) {}
