package com.bookos.backend.graph.dto;

public record GraphNodeResponse(
        String id,
        String type,
        String label,
        Long entityId) {}
