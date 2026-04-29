package com.bookos.backend.ai.service;

import com.bookos.backend.common.enums.AISuggestionType;
import com.bookos.backend.source.entity.SourceReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AISuggestionValidator {

    private static final int MAX_DRAFT_TEXT_LENGTH = 8000;
    private static final int MAX_TITLE_LENGTH = 240;

    private final ObjectMapper objectMapper;

    public AISuggestionValidator(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public AIDraft validate(AISuggestionType type, AIDraft draft, SourceReference sourceReference) {
        if (draft == null || !StringUtils.hasText(draft.draftText()) || !StringUtils.hasText(draft.draftJson())) {
            throw new IllegalArgumentException("AI draft text and JSON are required.");
        }
        if (draft.draftText().length() > MAX_DRAFT_TEXT_LENGTH) {
            throw new IllegalArgumentException("AI draft text is too long.");
        }

        JsonNode root = parseJson(draft.draftJson());
        List<String> warnings = new ArrayList<>(draft.validationWarnings());
        validateShape(type, root);
        validatePageNumbers(root, sourceReference);
        validateNoOverwriteTarget(root);

        return new AIDraft(draft.draftText().trim(), root.toString(), List.copyOf(warnings));
    }

    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception exception) {
            throw new IllegalArgumentException("AI draft JSON is invalid.");
        }
    }

    private void validateShape(AISuggestionType type, JsonNode root) {
        if (!root.isObject()) {
            throw new IllegalArgumentException("AI draft JSON must be an object.");
        }
        requireText(root, "provider", 80);
        requireText(root, "type", 80);

        switch (type) {
            case NOTE_SUMMARY -> requireText(root, "summary", 3000);
            case EXTRACT_ACTIONS -> validateArrayOfObjects(root, "actions", "title");
            case EXTRACT_CONCEPTS -> validateStringArray(root, "concepts");
            case SUGGEST_DESIGN_LENSES -> validateArrayOfObjects(root, "lenses", "name");
            case SUGGEST_PROJECT_APPLICATIONS -> validateArrayOfObjects(root, "applications", "title");
            case FORUM_THREAD_DRAFT -> {
                requireText(root, "title", MAX_TITLE_LENGTH);
                requireText(root, "bodyMarkdown", 6000);
            }
        }
    }

    private void requireText(JsonNode root, String field, int maxLength) {
        JsonNode value = root.path(field);
        if (!value.isTextual() || !StringUtils.hasText(value.asText())) {
            throw new IllegalArgumentException("AI draft JSON field '%s' is required.".formatted(field));
        }
        if (value.asText().length() > maxLength) {
            throw new IllegalArgumentException("AI draft JSON field '%s' is too long.".formatted(field));
        }
    }

    private void validateStringArray(JsonNode root, String field) {
        JsonNode values = root.path(field);
        if (!values.isArray() || values.isEmpty()) {
            throw new IllegalArgumentException("AI draft JSON field '%s' must be a non-empty array.".formatted(field));
        }
        for (JsonNode value : values) {
            if (!value.isTextual() || !StringUtils.hasText(value.asText()) || value.asText().length() > MAX_TITLE_LENGTH) {
                throw new IllegalArgumentException("AI draft JSON field '%s' contains an invalid value.".formatted(field));
            }
        }
    }

    private void validateArrayOfObjects(JsonNode root, String field, String requiredField) {
        JsonNode values = root.path(field);
        if (!values.isArray() || values.isEmpty()) {
            throw new IllegalArgumentException("AI draft JSON field '%s' must be a non-empty array.".formatted(field));
        }
        for (JsonNode value : values) {
            if (!value.isObject()) {
                throw new IllegalArgumentException("AI draft JSON field '%s' must contain objects.".formatted(field));
            }
            requireText(value, requiredField, MAX_TITLE_LENGTH);
            if (value.has("priority")) {
                String priority = value.path("priority").asText("");
                if (!List.of("LOW", "MEDIUM", "HIGH").contains(priority)) {
                    throw new IllegalArgumentException("AI draft JSON priority must be LOW, MEDIUM, or HIGH.");
                }
            }
        }
    }

    private void validatePageNumbers(JsonNode root, SourceReference sourceReference) {
        Iterator<JsonNode> iterator = root.isContainerNode() ? root.elements() : List.<JsonNode>of().iterator();
        while (iterator.hasNext()) {
            JsonNode child = iterator.next();
            validatePageNumbers(child, sourceReference);
        }

        validatePageField(root, "pageStart", sourceReference == null ? null : sourceReference.getPageStart());
        validatePageField(root, "pageEnd", sourceReference == null ? null : sourceReference.getPageEnd());
    }

    private void validatePageField(JsonNode root, String field, Integer allowedValue) {
        if (!root.isObject() || !root.has(field) || root.path(field).isNull()) {
            return;
        }
        if (!root.path(field).isIntegralNumber()) {
            throw new IllegalArgumentException("AI draft JSON page fields must be integers or null.");
        }
        if (allowedValue == null || root.path(field).asInt() != allowedValue) {
            throw new IllegalArgumentException("AI draft JSON cannot invent page numbers.");
        }
    }

    private void validateNoOverwriteTarget(JsonNode root) {
        if (root.has("overwrite") || root.has("overwriteTargetId") || root.has("targetEntityId")) {
            throw new IllegalArgumentException("AI draft JSON cannot request automatic overwrite targets.");
        }
        Iterator<JsonNode> iterator = root.isContainerNode() ? root.elements() : List.<JsonNode>of().iterator();
        while (iterator.hasNext()) {
            validateNoOverwriteTarget(iterator.next());
        }
    }
}
