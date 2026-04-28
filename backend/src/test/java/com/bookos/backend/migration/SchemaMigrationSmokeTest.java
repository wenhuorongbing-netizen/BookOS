package com.bookos.backend.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.bookos.backend.config.DemoDataInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class SchemaMigrationSmokeTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DemoDataInitializer demoDataInitializer;

    @Test
    void flywayCreatesCurrentSchemaAndCoreSeedsRemainIdempotent() {
        assertThat(countRows("flyway_schema_history")).isGreaterThanOrEqualTo(2);
        assertThat(tableExists("users")).isTrue();
        assertThat(tableExists("books")).isTrue();
        assertThat(tableExists("raw_captures")).isTrue();
        assertThat(tableExists("quotes")).isTrue();
        assertThat(tableExists("action_items")).isTrue();
        assertThat(tableExists("source_references")).isTrue();
        assertThat(tableExists("concepts")).isTrue();
        assertThat(tableExists("knowledge_objects")).isTrue();
        assertThat(tableExists("daily_sentences")).isTrue();
        assertThat(tableExists("forum_threads")).isTrue();
        assertThat(tableExists("ai_suggestions")).isTrue();

        long roleCount = countRows("roles");
        long forumCategoryCount = countRows("forum_categories");
        long templateCount = countRows("structured_post_templates");

        demoDataInitializer.run();

        assertThat(countRows("roles")).isEqualTo(roleCount);
        assertThat(countRows("forum_categories")).isEqualTo(forumCategoryCount);
        assertThat(countRows("structured_post_templates")).isEqualTo(templateCount);
    }

    private long countRows(String tableName) {
        Long count = jdbcTemplate.queryForObject("select count(*) from " + tableName, Long.class);
        return count == null ? 0 : count;
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where lower(table_name) = ?",
                Integer.class,
                tableName.toLowerCase());
        return count != null && count > 0;
    }
}
