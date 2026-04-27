package com.bookos.backend.graph.dto;

public record GraphEdgeResponse(
        String source,
        String target,
        String type) {}
