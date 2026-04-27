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
}
