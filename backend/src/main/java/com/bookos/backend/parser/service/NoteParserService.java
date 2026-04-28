package com.bookos.backend.parser.service;

import com.bookos.backend.common.enums.NoteBlockType;
import com.bookos.backend.parser.dto.ParsedNoteResponse;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NoteParserService {

    private static final Pattern EN_PAGE = Pattern.compile("\\b(?:p\\.?|pp\\.?|page)\\s*(\\d+)(?:\\s*[-\\u2013]\\s*(\\d+))?\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern ZH_PAGE_A = Pattern.compile("\\u7b2c\\s*(\\d+)(?:\\s*[-\\u2013]\\s*(\\d+))?\\s*\\u9875");
    private static final Pattern ZH_PAGE_B = Pattern.compile("\\u9875\\s*(\\d+)(?:\\s*[-\\u2013]\\s*(\\d+))?");
    private static final Pattern TAG = Pattern.compile("(?<![\\p{L}\\p{N}_/-])#([\\p{L}\\p{N}_-]+)");
    private static final Pattern CONCEPT = Pattern.compile("\\[\\[([^\\]]+)\\]\\]");

    private static final Map<String, NoteBlockType> MARKERS = markerMap();

    public ParsedNoteResponse parse(String rawText) {
        String raw = rawText == null ? "" : rawText.trim();
        List<String> warnings = new ArrayList<>();
        NoteBlockType type = detectType(raw, warnings);
        PageRange pageRange = detectPageRange(raw, warnings);
        List<String> tags = detectTags(raw);
        List<String> concepts = detectConcepts(raw);
        detectMalformedConceptLinks(raw, warnings);
        String cleanText = cleanText(raw);

        return new ParsedNoteResponse(
                type,
                pageRange.pageStart(),
                pageRange.pageEnd(),
                tags,
                concepts,
                cleanText,
                raw,
                List.copyOf(warnings));
    }

    private NoteBlockType detectType(String raw, List<String> warnings) {
        NoteBlockType found = NoteBlockType.NOTE;
        int firstIndex = Integer.MAX_VALUE;
        int foundCount = 0;
        for (Map.Entry<String, NoteBlockType> entry : MARKERS.entrySet()) {
            int index = raw.indexOf(entry.getKey());
            if (index >= 0) {
                foundCount++;
                if (index < firstIndex) {
                    firstIndex = index;
                    found = entry.getValue();
                }
            }
        }

        if (foundCount > 1) {
            warnings.add("Multiple emoji markers found; first marker in text was used as primary type.");
        }
        return found;
    }

    private PageRange detectPageRange(String raw, List<String> warnings) {
        List<PageRange> matches = new ArrayList<>();
        addPageMatch(matches, EN_PAGE.matcher(raw), warnings);
        addPageMatch(matches, ZH_PAGE_A.matcher(raw), warnings);
        addPageMatch(matches, ZH_PAGE_B.matcher(raw), warnings);

        if (matches.isEmpty()) {
            return new PageRange(null, null);
        }
        if (matches.size() > 1) {
            warnings.add("Multiple page references found; first page reference was used.");
        }
        return matches.get(0);
    }

    private void addPageMatch(List<PageRange> matches, Matcher matcher, List<String> warnings) {
        while (matcher.find()) {
            int start = Integer.parseInt(matcher.group(1));
            Integer end = matcher.group(2) == null ? null : Integer.parseInt(matcher.group(2));
            if (end != null && start > end) {
                warnings.add("Page range start is greater than end; page end was ignored.");
                end = null;
            }
            matches.add(new PageRange(start, end));
        }
    }

    private List<String> detectTags(String raw) {
        Set<String> values = new LinkedHashSet<>();
        Matcher matcher = TAG.matcher(raw);
        while (matcher.find()) {
            String value = matcher.group(1).trim().toLowerCase(Locale.ROOT);
            if (StringUtils.hasText(value)) {
                values.add(value);
            }
        }
        return List.copyOf(values);
    }

    private List<String> detectConcepts(String raw) {
        Map<String, String> values = new LinkedHashMap<>();
        Matcher matcher = CONCEPT.matcher(raw);
        while (matcher.find()) {
            String value = matcher.group(1).trim();
            if (StringUtils.hasText(value)) {
                values.putIfAbsent(value.toLowerCase(Locale.ROOT), value);
            }
        }
        return List.copyOf(values.values());
    }

    private void detectMalformedConceptLinks(String raw, List<String> warnings) {
        int openings = countOccurrences(raw, "[[");
        int closings = countOccurrences(raw, "]]");
        boolean emptyConcept = Pattern.compile("\\[\\[\\s*\\]\\]").matcher(raw).find();
        if (openings != closings || emptyConcept) {
            warnings.add("Malformed concept link found; use [[Concept Name]] syntax.");
        }
    }

    private int countOccurrences(String raw, String token) {
        int count = 0;
        int index = 0;
        while ((index = raw.indexOf(token, index)) >= 0) {
            count++;
            index += token.length();
        }
        return count;
    }

    private String cleanText(String raw) {
        String result = raw;
        for (String marker : MARKERS.keySet()) {
            result = result.replace(marker, " ");
        }
        result = EN_PAGE.matcher(result).replaceAll(" ");
        result = ZH_PAGE_A.matcher(result).replaceAll(" ");
        result = ZH_PAGE_B.matcher(result).replaceAll(" ");
        result = TAG.matcher(result).replaceAll(" ");
        result = CONCEPT.matcher(result).replaceAll("$1");
        return result.replaceAll("\\s+", " ").trim();
    }

    private static Map<String, NoteBlockType> markerMap() {
        Map<String, NoteBlockType> markers = new LinkedHashMap<>();
        markers.put("\uD83D\uDCA1", NoteBlockType.INSPIRATION);
        markers.put("\uD83D\uDD11", NoteBlockType.KEY_ARGUMENT);
        markers.put("\uD83D\uDCAC", NoteBlockType.QUOTE);
        markers.put("\uD83D\uDDE3\uFE0F", NoteBlockType.DISCUSSION_POINT);
        markers.put("\uD83D\uDDE3", NoteBlockType.DISCUSSION_POINT);
        markers.put("\uD83E\uDD2F", NoteBlockType.MIND_BLOWING_IDEA);
        markers.put("\u2705", NoteBlockType.ACTION_ITEM);
        markers.put("\u2753", NoteBlockType.QUESTION);
        markers.put("\uD83E\uDDE0", NoteBlockType.MENTAL_MODEL);
        markers.put("\uD83E\uDDE9", NoteBlockType.RELATED_CONCEPT);
        markers.put("\u26A0\uFE0F", NoteBlockType.WARNING);
        markers.put("\u26A0", NoteBlockType.WARNING);
        markers.put("\uD83D\uDCCC", NoteBlockType.IMPORTANT);
        markers.put("\uD83E\uDDEA", NoteBlockType.EXPERIMENT);
        markers.put("\uD83D\uDCDD", NoteBlockType.PERSONAL_REFLECTION);
        markers.put("\uD83D\uDCCA", NoteBlockType.DATA_STATISTIC);
        markers.put("\uD83D\uDD17", NoteBlockType.LINK);
        markers.put("\uD83E\uDDED", NoteBlockType.READING_DIRECTION);
        return markers;
    }

    private record PageRange(Integer pageStart, Integer pageEnd) {}
}
