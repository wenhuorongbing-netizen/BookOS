package com.bookos.backend.admin.dto;

import com.bookos.backend.common.enums.SourceConfidence;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record OntologySeedConceptRequest(
        @NotBlank(message = "Concept title is required.")
        @Size(max = 180, message = "Concept title must be at most 180 characters.")
        String title,

        @Size(max = 10000, message = "Description must be at most 10000 characters.")
        String description,

        @NotBlank(message = "Ontology layer is required.")
        @Size(max = 80, message = "Ontology layer must be at most 80 characters.")
        String layer,

        List<@Size(max = 80, message = "Tag must be at most 80 characters.") String> tags,

        @Size(max = 220, message = "Source book title must be at most 220 characters.")
        String sourceBookTitle,

        SourceConfidence sourceConfidence) {}
