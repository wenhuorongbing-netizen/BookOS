package com.bookos.backend.ai.service;

import com.bookos.backend.common.enums.AISuggestionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class MockAIProvider implements AIProvider {

    private final ObjectMapper objectMapper;

    @Override
    public String providerName() {
        return "MockAIProvider";
    }

    @Override
    public AIDraft generate(AISuggestionType type, String sourceText, String sourceTitle) {
        String cleanSource = cleanSource(sourceText);
        String title = StringUtils.hasText(sourceTitle) ? sourceTitle : "selected BookOS source";
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("provider", providerName());
        payload.put("sourceTitle", title);
        payload.put("sourceExcerpt", excerpt(cleanSource, 280));
        payload.put("type", type.name());

        String draftText;
        if (type == AISuggestionType.NOTE_SUMMARY) {
            draftText = "Draft summary: " + excerpt(cleanSource, 220);
            payload.put("summary", draftText);
        } else if (type == AISuggestionType.EXTRACT_ACTIONS) {
            String action = "Review and apply: " + excerpt(cleanSource, 160);
            draftText = action;
            payload.put("actions", List.of(Map.of("title", action, "priority", "MEDIUM")));
        } else if (type == AISuggestionType.EXTRACT_CONCEPTS) {
            List<String> concepts = deterministicConcepts(cleanSource);
            draftText = "Draft concepts: " + String.join(", ", concepts);
            payload.put("concepts", concepts);
        } else if (type == AISuggestionType.SUGGEST_DESIGN_LENSES) {
            String lensName = "Lens of " + deterministicConcepts(cleanSource).getFirst();
            draftText = "Draft design lens: " + lensName;
            payload.put("lenses", List.of(Map.of("name", lensName, "question", "How does this source change the player's decision space?")));
        } else if (type == AISuggestionType.SUGGEST_PROJECT_APPLICATIONS) {
            String titleText = "Apply insight: " + excerpt(cleanSource, 120);
            draftText = titleText;
            payload.put("applications", List.of(Map.of("title", titleText, "applicationType", "PROJECT_APPLICATION")));
        } else {
            String titleText = "Discuss source-backed idea";
            draftText = titleText + ": " + excerpt(cleanSource, 180);
            payload.put("title", titleText);
            payload.put("bodyMarkdown", "Source-backed discussion draft:\n\n" + excerpt(cleanSource, 480));
        }

        return new AIDraft(draftText, writeJson(payload));
    }

    private List<String> deterministicConcepts(String sourceText) {
        String[] words = sourceText.replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}\\s-]", " ")
                .split("\\s+");
        java.util.LinkedHashSet<String> concepts = new java.util.LinkedHashSet<>();
        for (String word : words) {
            String clean = word.trim();
            if (clean.length() >= 5) {
                concepts.add(Character.toUpperCase(clean.charAt(0)) + clean.substring(1).toLowerCase(java.util.Locale.ROOT));
            }
            if (concepts.size() >= 5) {
                break;
            }
        }
        if (concepts.isEmpty()) {
            concepts.add("Reading Insight");
        }
        return List.copyOf(concepts);
    }

    private String cleanSource(String sourceText) {
        if (!StringUtils.hasText(sourceText)) {
            return "No source-backed text is available yet. Add notes, captures, quotes, or source references to generate stronger drafts.";
        }
        return sourceText.replaceAll("\\s+", " ").trim();
    }

    private String excerpt(String value, int maxLength) {
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private String writeJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Mock AI payload could not be serialized.", exception);
        }
    }
}
