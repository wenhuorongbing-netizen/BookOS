package com.bookos.backend.learning.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ReadingSessionFinishRequest(
        @Min(0) Integer endPage,
        @Min(0) Integer minutesRead,
        @Min(0) Integer notesCount,
        @Min(0) Integer capturesCount,
        @Size(max = 10000) String reflection) {}
