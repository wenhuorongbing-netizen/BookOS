package com.bookos.backend.parser;

import static org.assertj.core.api.Assertions.assertThat;

import com.bookos.backend.common.enums.NoteBlockType;
import com.bookos.backend.parser.service.NoteParserService;
import org.junit.jupiter.api.Test;

class NoteParserServiceTest {

    private final NoteParserService parser = new NoteParserService();

    @Test
    void parsesQuoteWithEnglishPageTagAndConcept() {
        var raw = "\uD83D\uDCAC p.42 \"The impediment to action advances action.\" #quote [[Stoicism]]";

        var parsed = parser.parse(raw);

        assertThat(parsed.type()).isEqualTo(NoteBlockType.QUOTE);
        assertThat(parsed.pageStart()).isEqualTo(42);
        assertThat(parsed.pageEnd()).isNull();
        assertThat(parsed.tags()).containsExactly("quote");
        assertThat(parsed.concepts()).containsExactly("Stoicism");
        assertThat(parsed.rawText()).isEqualTo(raw);
        assertThat(parsed.cleanText()).contains("The impediment to action advances action.");
    }

    @Test
    void parsesActionItemWithChinesePageTagAndConcept() {
        var raw = "\u2705 \u7B2C80\u9875 Try this method tomorrow. #todo [[Habit Design]]";

        var parsed = parser.parse(raw);

        assertThat(parsed.type()).isEqualTo(NoteBlockType.ACTION_ITEM);
        assertThat(parsed.pageStart()).isEqualTo(80);
        assertThat(parsed.tags()).containsExactly("todo");
        assertThat(parsed.concepts()).containsExactly("Habit Design");
        assertThat(parsed.cleanText()).contains("Try this method tomorrow.");
    }

    @Test
    void reportsInvalidPageRangeWithoutInventingEndPage() {
        var raw = "\uD83D\uDCA1 pp.90-80 Inverted range should be kept visible. [[Flow]]";

        var parsed = parser.parse(raw);

        assertThat(parsed.type()).isEqualTo(NoteBlockType.INSPIRATION);
        assertThat(parsed.pageStart()).isEqualTo(90);
        assertThat(parsed.pageEnd()).isNull();
        assertThat(parsed.warnings()).contains("Page range start is greater than end; page end was ignored.");
    }

    @Test
    void parsesMixedChineseAndEnglishPageMarkersWithoutInventingExtraPage() {
        var raw = "\uD83D\uDCA1 page 12 \u7B2C20\u9875 Compare pacing notes. #loop [[Core Loop]]";

        var parsed = parser.parse(raw);

        assertThat(parsed.type()).isEqualTo(NoteBlockType.INSPIRATION);
        assertThat(parsed.pageStart()).isEqualTo(12);
        assertThat(parsed.pageEnd()).isNull();
        assertThat(parsed.tags()).containsExactly("loop");
        assertThat(parsed.concepts()).containsExactly("Core Loop");
        assertThat(parsed.warnings()).contains("Multiple page references found; first page reference was used.");
    }

    @Test
    void parsesMultipleTagsAndConceptsWithStableDeduping() {
        var raw = "\uD83E\uDDE9 p.14 System loop #Game-Feel #prototype #game-feel [[Flow]] [[flow]] [[Feedback Loop]]";

        var parsed = parser.parse(raw);

        assertThat(parsed.type()).isEqualTo(NoteBlockType.RELATED_CONCEPT);
        assertThat(parsed.tags()).containsExactly("game-feel", "prototype");
        assertThat(parsed.concepts()).containsExactly("Flow", "Feedback Loop");
        assertThat(parsed.cleanText()).contains("System loop").contains("Flow").contains("Feedback Loop");
    }

    @Test
    void usesFirstEmojiMarkerInTextWhenMultipleMarkersExist() {
        var raw = "\u26A0\uFE0F \u2705 p.9 Challenge this assumption before making it an action. #risk";

        var parsed = parser.parse(raw);

        assertThat(parsed.type()).isEqualTo(NoteBlockType.WARNING);
        assertThat(parsed.pageStart()).isEqualTo(9);
        assertThat(parsed.tags()).containsExactly("risk");
        assertThat(parsed.warnings()).contains("Multiple emoji markers found; first marker in text was used as primary type.");
    }

    @Test
    void leavesPageNullWhenNoPageNumberExists() {
        var raw = "\u2753 How does this mechanic change player motivation? [[Player Motivation]]";

        var parsed = parser.parse(raw);

        assertThat(parsed.type()).isEqualTo(NoteBlockType.QUESTION);
        assertThat(parsed.pageStart()).isNull();
        assertThat(parsed.pageEnd()).isNull();
        assertThat(parsed.concepts()).containsExactly("Player Motivation");
    }

    @Test
    void reportsMalformedConceptBracketsWithoutParsingConcept() {
        var raw = "\uD83D\uDCA1 p.22 This has a malformed concept [[Core Loop #prototype";

        var parsed = parser.parse(raw);

        assertThat(parsed.type()).isEqualTo(NoteBlockType.INSPIRATION);
        assertThat(parsed.pageStart()).isEqualTo(22);
        assertThat(parsed.concepts()).isEmpty();
        assertThat(parsed.tags()).containsExactly("prototype");
        assertThat(parsed.warnings()).contains("Malformed concept link found; use [[Concept Name]] syntax.");
    }
}
