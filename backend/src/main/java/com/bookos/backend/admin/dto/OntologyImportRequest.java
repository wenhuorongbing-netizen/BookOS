package com.bookos.backend.admin.dto;

import jakarta.validation.Valid;
import java.util.List;

public record OntologyImportRequest(
        List<@Valid OntologySeedBookRequest> books,
        List<@Valid OntologySeedConceptRequest> concepts,
        List<@Valid OntologySeedKnowledgeObjectRequest> knowledgeObjects) {}
