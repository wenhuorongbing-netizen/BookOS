package com.bookos.backend.project.dto;

public record ProjectWizardCreatedRecordResponse(
        String type,
        Long id,
        String title,
        boolean sourcePreserved) {}
