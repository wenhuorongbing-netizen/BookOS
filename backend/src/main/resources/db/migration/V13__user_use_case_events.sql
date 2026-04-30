CREATE TABLE user_use_case_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    context_type VARCHAR(80) DEFAULT NULL,
    context_id VARCHAR(120) DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_use_case_events_user_type (user_id, event_type),
    KEY idx_use_case_events_context (context_type, context_id),
    CONSTRAINT fk_use_case_events_user FOREIGN KEY (user_id) REFERENCES users (id)
);
