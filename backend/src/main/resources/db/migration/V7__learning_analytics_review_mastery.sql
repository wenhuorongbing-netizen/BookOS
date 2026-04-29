CREATE TABLE reading_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    captures_count INT NOT NULL,
    ended_at DATETIME(6) DEFAULT NULL,
    end_page INT DEFAULT NULL,
    minutes_read INT DEFAULT NULL,
    notes_count INT NOT NULL,
    reflection LONGTEXT DEFAULT NULL,
    started_at DATETIME(6) NOT NULL,
    start_page INT DEFAULT NULL,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_reading_sessions_user_started (user_id, started_at),
    KEY idx_reading_sessions_book_started (book_id, started_at),
    CONSTRAINT fk_reading_sessions_book FOREIGN KEY (book_id) REFERENCES books (id),
    CONSTRAINT fk_reading_sessions_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE review_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    completed_at DATETIME(6) DEFAULT NULL,
    mode VARCHAR(64) NOT NULL,
    scope_id BIGINT DEFAULT NULL,
    scope_type VARCHAR(64) NOT NULL,
    started_at DATETIME(6) NOT NULL,
    summary LONGTEXT DEFAULT NULL,
    title VARCHAR(220) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_review_sessions_user_started (user_id, started_at),
    CONSTRAINT fk_review_sessions_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE TABLE review_items (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    confidence_score INT DEFAULT NULL,
    prompt LONGTEXT NOT NULL,
    status VARCHAR(64) NOT NULL,
    target_id BIGINT NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    user_response LONGTEXT DEFAULT NULL,
    review_session_id BIGINT NOT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_review_items_session (review_session_id),
    KEY idx_review_items_target (target_type, target_id),
    KEY idx_review_items_source (source_reference_id),
    CONSTRAINT fk_review_items_session FOREIGN KEY (review_session_id) REFERENCES review_sessions (id),
    CONSTRAINT fk_review_items_source FOREIGN KEY (source_reference_id) REFERENCES source_references (id)
);

CREATE TABLE knowledge_mastery (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    familiarity_score INT NOT NULL,
    last_reviewed_at DATETIME(6) DEFAULT NULL,
    next_review_at DATETIME(6) DEFAULT NULL,
    target_id BIGINT NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    usefulness_score INT NOT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_mastery_user_target (user_id, target_type, target_id),
    KEY idx_mastery_user_updated (user_id, updated_at),
    KEY idx_mastery_user_next (user_id, next_review_at),
    KEY idx_mastery_source (source_reference_id),
    CONSTRAINT fk_mastery_source FOREIGN KEY (source_reference_id) REFERENCES source_references (id),
    CONSTRAINT fk_mastery_user FOREIGN KEY (user_id) REFERENCES users (id)
);
