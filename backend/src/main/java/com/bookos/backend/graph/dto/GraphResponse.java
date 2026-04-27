package com.bookos.backend.graph.dto;

import java.util.List;

public record GraphResponse(
        List<GraphNodeResponse> nodes,
        List<GraphEdgeResponse> edges) {}
