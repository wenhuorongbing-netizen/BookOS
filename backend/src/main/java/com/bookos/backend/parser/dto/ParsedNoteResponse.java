package com.bookos.backend.parser.dto;

import com.bookos.backend.common.enums.NoteBlockType;
import java.util.List;

public record ParsedNoteResponse(
        NoteBlockType type,
        Integer pageStart,
        Integer pageEnd,
        List<String> tags,
        List<String> concepts,
        String cleanText,
        String rawText,
        List<String> warnings) {}
