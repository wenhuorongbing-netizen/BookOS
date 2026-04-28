CREATE TABLE roles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    name ENUM('ADMIN','MODERATOR','USER') NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_roles_name (name)
);

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    email VARCHAR(190) NOT NULL,
    enabled BIT(1) NOT NULL,
    password_hash VARCHAR(120) NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    KEY idx_users_role (role_id),
    CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

CREATE TABLE user_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    bio VARCHAR(500) DEFAULT NULL,
    display_name VARCHAR(120) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_profiles_user (user_id),
    CONSTRAINT fk_user_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE authors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    name VARCHAR(180) NOT NULL,
    slug VARCHAR(180) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_authors_name (name),
    UNIQUE KEY uk_authors_slug (slug)
);

CREATE TABLE tags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(120) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_tags_name (name),
    UNIQUE KEY uk_tags_slug (slug)
);

CREATE TABLE books (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    category VARCHAR(120) DEFAULT NULL,
    cover_url VARCHAR(500) DEFAULT NULL,
    description VARCHAR(3000) DEFAULT NULL,
    isbn VARCHAR(50) DEFAULT NULL,
    publication_year INT DEFAULT NULL,
    publisher VARCHAR(180) DEFAULT NULL,
    subtitle VARCHAR(220) DEFAULT NULL,
    title VARCHAR(220) NOT NULL,
    visibility ENUM('PRIVATE','PUBLIC','SHARED') NOT NULL,
    owner_user_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_books_owner (owner_user_id),
    CONSTRAINT fk_books_owner FOREIGN KEY (owner_user_id) REFERENCES users (id)
);

CREATE TABLE book_authors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    display_order INT NOT NULL,
    author_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_book_authors_book_author (book_id, author_id),
    KEY idx_book_authors_author (author_id),
    CONSTRAINT fk_book_authors_author FOREIGN KEY (author_id) REFERENCES authors (id),
    CONSTRAINT fk_book_authors_book FOREIGN KEY (book_id) REFERENCES books (id)
);

CREATE TABLE book_tags (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    book_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_book_tags_book_tag (book_id, tag_id),
    KEY idx_book_tags_tag (tag_id),
    CONSTRAINT fk_book_tags_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_book_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
);

CREATE TABLE user_books (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    ownership_status ENUM('BORROWED','OWNED','SAMPLE','SUBSCRIPTION','WISHLIST') NOT NULL,
    progress_percent INT NOT NULL,
    rating INT DEFAULT NULL,
    reading_format ENUM('AUDIOBOOK','EBOOK','OTHER','PDF','PHYSICAL','WEB') NOT NULL,
    reading_status ENUM('ANTI_LIBRARY','BACKLOG','COMPLETED','CURRENTLY_READING','DROPPED','PAUSED','REFERENCE') NOT NULL,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_user_books_user_book (user_id, book_id),
    KEY idx_user_books_book (book_id),
    CONSTRAINT fk_user_books_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_user_books_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE book_notes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    archived BIT(1) NOT NULL,
    markdown LONGTEXT NOT NULL,
    three_sentence_summary VARCHAR(1000) DEFAULT NULL,
    title VARCHAR(220) NOT NULL,
    visibility ENUM('PRIVATE','PUBLIC','SHARED') NOT NULL,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_book_notes_book (book_id),
    KEY idx_book_notes_user (user_id),
    CONSTRAINT fk_book_notes_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_book_notes_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE note_blocks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    block_type ENUM('ACTION','ACTION_ITEM','DATA_STATISTIC','DISCUSSION_POINT','EXPERIMENT','IDEA','IMPORTANT','INSPIRATION','KEY_ARGUMENT','LINK','MENTAL_MODEL','MIND_BLOWING_IDEA','NOTE','PERSONAL_REFLECTION','QUESTION','QUOTE','READING_DIRECTION','REFERENCE','RELATED_CONCEPT','SUMMARY','WARNING') NOT NULL,
    markdown LONGTEXT NOT NULL,
    page_end INT DEFAULT NULL,
    page_start INT DEFAULT NULL,
    parser_warnings VARCHAR(1000) DEFAULT NULL,
    plain_text VARCHAR(5000) DEFAULT NULL,
    raw_text LONGTEXT NOT NULL,
    sort_order INT NOT NULL,
    book_id BIGINT NOT NULL,
    note_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_note_blocks_book (book_id),
    KEY idx_note_blocks_note (note_id),
    KEY idx_note_blocks_user (user_id),
    CONSTRAINT fk_note_blocks_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_note_blocks_note FOREIGN KEY (note_id) REFERENCES book_notes (id),
    CONSTRAINT fk_note_blocks_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE raw_captures (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    clean_text LONGTEXT DEFAULT NULL,
    concepts_json LONGTEXT DEFAULT NULL,
    converted_entity_id BIGINT DEFAULT NULL,
    converted_entity_type VARCHAR(80) DEFAULT NULL,
    page_end INT DEFAULT NULL,
    page_start INT DEFAULT NULL,
    parsed_type ENUM('ACTION','ACTION_ITEM','DATA_STATISTIC','DISCUSSION_POINT','EXPERIMENT','IDEA','IMPORTANT','INSPIRATION','KEY_ARGUMENT','LINK','MENTAL_MODEL','MIND_BLOWING_IDEA','NOTE','PERSONAL_REFLECTION','QUESTION','QUOTE','READING_DIRECTION','REFERENCE','RELATED_CONCEPT','SUMMARY','WARNING') NOT NULL,
    parser_warnings_json LONGTEXT DEFAULT NULL,
    raw_text LONGTEXT NOT NULL,
    status ENUM('ARCHIVED','CONVERTED','DISCARDED','INBOX') NOT NULL,
    tags_json LONGTEXT DEFAULT NULL,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_raw_captures_book (book_id),
    KEY idx_raw_captures_user (user_id),
    CONSTRAINT fk_raw_captures_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_raw_captures_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE source_references (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    chapter_id BIGINT DEFAULT NULL,
    location_label VARCHAR(255) DEFAULT NULL,
    page_end INT DEFAULT NULL,
    page_start INT DEFAULT NULL,
    raw_capture_id BIGINT DEFAULT NULL,
    source_confidence ENUM('HIGH','LOW','MEDIUM') NOT NULL,
    source_text LONGTEXT DEFAULT NULL,
    source_type VARCHAR(64) NOT NULL,
    book_id BIGINT NOT NULL,
    note_id BIGINT DEFAULT NULL,
    note_block_id BIGINT DEFAULT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_source_references_book (book_id),
    KEY idx_source_references_note (note_id),
    KEY idx_source_references_note_block (note_block_id),
    KEY idx_source_references_user (user_id),
    CONSTRAINT fk_source_references_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_source_references_note FOREIGN KEY (note_id) REFERENCES book_notes (id),
    CONSTRAINT fk_source_references_note_block FOREIGN KEY (note_block_id) REFERENCES note_blocks (id),
    CONSTRAINT fk_source_references_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE concepts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    archived BIT(1) NOT NULL,
    description LONGTEXT DEFAULT NULL,
    mention_count INT NOT NULL,
    name VARCHAR(180) NOT NULL,
    slug VARCHAR(220) NOT NULL,
    visibility ENUM('PRIVATE','PUBLIC','SHARED') NOT NULL,
    first_book_id BIGINT DEFAULT NULL,
    first_source_reference_id BIGINT DEFAULT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_concepts_user_slug (user_id, slug),
    KEY idx_concepts_user_slug (user_id, slug),
    KEY idx_concepts_user_book (user_id, first_book_id),
    KEY idx_concepts_first_book (first_book_id),
    KEY idx_concepts_first_source_reference (first_source_reference_id),
    CONSTRAINT fk_concepts_first_book FOREIGN KEY (first_book_id) REFERENCES books (id),
    CONSTRAINT fk_concepts_first_source_reference FOREIGN KEY (first_source_reference_id) REFERENCES source_references (id),
    CONSTRAINT fk_concepts_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE knowledge_objects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    archived BIT(1) NOT NULL,
    description LONGTEXT DEFAULT NULL,
    slug VARCHAR(260) NOT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    tags_json LONGTEXT DEFAULT NULL,
    title VARCHAR(220) NOT NULL,
    type ENUM('ANTI_PATTERN','CHECKLIST','CONCEPT','CROSS_BOOK_SYNTHESIS','DESIGN_LENS','DIAGNOSTIC_QUESTION','EXAMPLE_CASE','EXERCISE','LENS','MECHANIC','METHOD','METHOD_PATTERN','PATTERN','PRINCIPLE','PROTOTYPE_TASK','QUESTION') NOT NULL,
    visibility ENUM('PRIVATE','PUBLIC','SHARED') NOT NULL,
    book_id BIGINT DEFAULT NULL,
    concept_id BIGINT DEFAULT NULL,
    note_id BIGINT DEFAULT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_knowledge_user_type_slug (user_id, type, slug),
    KEY idx_knowledge_user_type (user_id, type),
    KEY idx_knowledge_user_book (user_id, book_id),
    KEY idx_knowledge_user_concept (user_id, concept_id),
    KEY idx_knowledge_book (book_id),
    KEY idx_knowledge_concept (concept_id),
    KEY idx_knowledge_note (note_id),
    CONSTRAINT fk_knowledge_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_knowledge_concept FOREIGN KEY (concept_id) REFERENCES concepts (id),
    CONSTRAINT fk_knowledge_note FOREIGN KEY (note_id) REFERENCES book_notes (id),
    CONSTRAINT fk_knowledge_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE quotes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    archived BIT(1) NOT NULL,
    attribution VARCHAR(220) DEFAULT NULL,
    concepts_json LONGTEXT DEFAULT NULL,
    page_end INT DEFAULT NULL,
    page_start INT DEFAULT NULL,
    raw_capture_id BIGINT DEFAULT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    tags_json LONGTEXT DEFAULT NULL,
    text LONGTEXT NOT NULL,
    visibility ENUM('PRIVATE','PUBLIC','SHARED') NOT NULL,
    book_id BIGINT NOT NULL,
    note_id BIGINT DEFAULT NULL,
    note_block_id BIGINT DEFAULT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_quotes_user_book (user_id, book_id),
    KEY idx_quotes_user_source (user_id, source_reference_id),
    KEY idx_quotes_book (book_id),
    KEY idx_quotes_note (note_id),
    KEY idx_quotes_note_block (note_block_id),
    CONSTRAINT fk_quotes_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_quotes_note FOREIGN KEY (note_id) REFERENCES book_notes (id),
    CONSTRAINT fk_quotes_note_block FOREIGN KEY (note_block_id) REFERENCES note_blocks (id),
    CONSTRAINT fk_quotes_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE action_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    archived BIT(1) NOT NULL,
    completed_at DATETIME(6) DEFAULT NULL,
    description LONGTEXT DEFAULT NULL,
    page_end INT DEFAULT NULL,
    page_start INT DEFAULT NULL,
    priority ENUM('HIGH','LOW','MEDIUM') NOT NULL,
    raw_capture_id BIGINT DEFAULT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    title VARCHAR(220) NOT NULL,
    visibility ENUM('PRIVATE','PUBLIC','SHARED') NOT NULL,
    book_id BIGINT NOT NULL,
    note_id BIGINT DEFAULT NULL,
    note_block_id BIGINT DEFAULT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_action_items_user_book (user_id, book_id),
    KEY idx_action_items_user_source (user_id, source_reference_id),
    KEY idx_action_items_book (book_id),
    KEY idx_action_items_note (note_id),
    KEY idx_action_items_note_block (note_block_id),
    CONSTRAINT fk_action_items_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_action_items_note FOREIGN KEY (note_id) REFERENCES book_notes (id),
    CONSTRAINT fk_action_items_note_block FOREIGN KEY (note_block_id) REFERENCES note_blocks (id),
    CONSTRAINT fk_action_items_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE entity_links (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    relation_type VARCHAR(80) NOT NULL,
    source_id BIGINT NOT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    source_type VARCHAR(64) NOT NULL,
    target_id BIGINT NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_entity_links_unique_relation (user_id, source_type, source_id, target_type, target_id, relation_type),
    KEY idx_entity_links_source (user_id, source_type, source_id),
    KEY idx_entity_links_target (user_id, target_type, target_id),
    CONSTRAINT fk_entity_links_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE daily_sentences (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    active BIT(1) NOT NULL,
    attribution VARCHAR(220) DEFAULT NULL,
    daily_date DATE NOT NULL,
    page_end INT DEFAULT NULL,
    page_start INT DEFAULT NULL,
    skipped BIT(1) NOT NULL,
    source_backed BIT(1) NOT NULL,
    source_id BIGINT DEFAULT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    source_type VARCHAR(64) DEFAULT NULL,
    text LONGTEXT NOT NULL,
    book_id BIGINT DEFAULT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_daily_sentences_user_day_active (user_id, daily_date, active),
    KEY idx_daily_sentences_user_source (user_id, source_type, source_id),
    KEY idx_daily_sentences_book (book_id),
    CONSTRAINT fk_daily_sentences_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_daily_sentences_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE daily_design_prompts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    active BIT(1) NOT NULL,
    daily_date DATE NOT NULL,
    question LONGTEXT NOT NULL,
    skipped BIT(1) NOT NULL,
    source_id BIGINT DEFAULT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    source_title VARCHAR(220) DEFAULT NULL,
    source_type VARCHAR(64) DEFAULT NULL,
    template_prompt BIT(1) NOT NULL,
    book_id BIGINT DEFAULT NULL,
    knowledge_object_id BIGINT DEFAULT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_daily_prompts_user_day_active (user_id, daily_date, active),
    KEY idx_daily_prompts_user_source (user_id, source_type, source_id),
    KEY idx_daily_prompts_book (book_id),
    KEY idx_daily_prompts_knowledge_object (knowledge_object_id),
    CONSTRAINT fk_daily_prompts_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_daily_prompts_knowledge_object FOREIGN KEY (knowledge_object_id) REFERENCES knowledge_objects (id),
    CONSTRAINT fk_daily_prompts_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE daily_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    action VARCHAR(32) NOT NULL,
    daily_design_prompt_id BIGINT DEFAULT NULL,
    daily_sentence_id BIGINT DEFAULT NULL,
    daily_date DATE NOT NULL,
    item_type VARCHAR(32) NOT NULL,
    source_id BIGINT DEFAULT NULL,
    source_type VARCHAR(64) DEFAULT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_daily_history_user_day_type (user_id, daily_date, item_type),
    CONSTRAINT fk_daily_history_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE daily_reflections (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    daily_design_prompt_id BIGINT DEFAULT NULL,
    daily_sentence_id BIGINT DEFAULT NULL,
    daily_date DATE NOT NULL,
    reflection_text LONGTEXT NOT NULL,
    target_type VARCHAR(32) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_daily_reflections_user_day (user_id, daily_date),
    CONSTRAINT fk_daily_reflections_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE forum_categories (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    description VARCHAR(1000) DEFAULT NULL,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL,
    sort_order INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_forum_categories_name (name),
    UNIQUE KEY uk_forum_categories_slug (slug)
);

CREATE TABLE structured_post_templates (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    body_markdown_template LONGTEXT NOT NULL,
    default_related_entity_type VARCHAR(64) DEFAULT NULL,
    description VARCHAR(1000) DEFAULT NULL,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL,
    sort_order INT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_structured_post_templates_name (name),
    UNIQUE KEY uk_structured_post_templates_slug (slug)
);

CREATE TABLE forum_threads (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    body_markdown LONGTEXT NOT NULL,
    related_entity_id BIGINT DEFAULT NULL,
    related_entity_type VARCHAR(64) DEFAULT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    status ENUM('ACTIVE','ARCHIVED','CLOSED') NOT NULL,
    title VARCHAR(220) NOT NULL,
    visibility ENUM('PRIVATE','PUBLIC','SHARED') NOT NULL,
    author_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    related_book_id BIGINT DEFAULT NULL,
    related_concept_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_forum_threads_category_status (category_id, status),
    KEY idx_forum_threads_author_status (author_id, status),
    KEY idx_forum_threads_related (related_entity_type, related_entity_id),
    KEY idx_forum_threads_related_book (related_book_id),
    KEY idx_forum_threads_related_concept (related_concept_id),
    CONSTRAINT fk_forum_threads_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_forum_threads_category FOREIGN KEY (category_id) REFERENCES forum_categories (id),
    CONSTRAINT fk_forum_threads_related_book FOREIGN KEY (related_book_id) REFERENCES books (id),
    CONSTRAINT fk_forum_threads_related_concept FOREIGN KEY (related_concept_id) REFERENCES concepts (id)
);

CREATE TABLE forum_comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    archived BIT(1) NOT NULL,
    body_markdown LONGTEXT NOT NULL,
    parent_comment_id BIGINT DEFAULT NULL,
    author_id BIGINT NOT NULL,
    thread_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_forum_comments_thread_archived (thread_id, archived),
    KEY idx_forum_comments_author (author_id),
    CONSTRAINT fk_forum_comments_author FOREIGN KEY (author_id) REFERENCES users (id),
    CONSTRAINT fk_forum_comments_thread FOREIGN KEY (thread_id) REFERENCES forum_threads (id)
);

CREATE TABLE forum_likes (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    thread_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_forum_like_thread_user (thread_id, user_id),
    KEY idx_forum_likes_user (user_id),
    CONSTRAINT fk_forum_likes_thread FOREIGN KEY (thread_id) REFERENCES forum_threads (id),
    CONSTRAINT fk_forum_likes_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE forum_bookmarks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    thread_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_forum_bookmark_thread_user (thread_id, user_id),
    KEY idx_forum_bookmarks_user (user_id),
    CONSTRAINT fk_forum_bookmarks_thread FOREIGN KEY (thread_id) REFERENCES forum_threads (id),
    CONSTRAINT fk_forum_bookmarks_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE forum_reports (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    details LONGTEXT DEFAULT NULL,
    reason VARCHAR(120) DEFAULT NULL,
    resolved BIT(1) NOT NULL,
    reporter_id BIGINT NOT NULL,
    thread_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_forum_reports_reporter (reporter_id),
    KEY idx_forum_reports_thread (thread_id),
    CONSTRAINT fk_forum_reports_reporter FOREIGN KEY (reporter_id) REFERENCES users (id),
    CONSTRAINT fk_forum_reports_thread FOREIGN KEY (thread_id) REFERENCES forum_threads (id)
);

CREATE TABLE ai_interactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    input_json LONGTEXT DEFAULT NULL,
    provider_name VARCHAR(80) NOT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    suggestion_type ENUM('EXTRACT_ACTIONS','EXTRACT_CONCEPTS','NOTE_SUMMARY') NOT NULL,
    book_id BIGINT DEFAULT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_interactions_book (book_id),
    KEY idx_ai_interactions_user (user_id),
    CONSTRAINT fk_ai_interactions_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_ai_interactions_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE ai_suggestions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    accepted_at DATETIME(6) DEFAULT NULL,
    draft_json LONGTEXT NOT NULL,
    draft_text LONGTEXT NOT NULL,
    provider_name VARCHAR(80) NOT NULL,
    rejected_at DATETIME(6) DEFAULT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    status ENUM('ACCEPTED','DRAFT','REJECTED') NOT NULL,
    suggestion_type ENUM('EXTRACT_ACTIONS','EXTRACT_CONCEPTS','NOTE_SUMMARY') NOT NULL,
    book_id BIGINT DEFAULT NULL,
    interaction_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_ai_suggestions_book (book_id),
    KEY idx_ai_suggestions_interaction (interaction_id),
    KEY idx_ai_suggestions_user (user_id),
    CONSTRAINT fk_ai_suggestions_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_ai_suggestions_interaction FOREIGN KEY (interaction_id) REFERENCES ai_interactions (id),
    CONSTRAINT fk_ai_suggestions_user FOREIGN KEY (user_id) REFERENCES users (id)
);
