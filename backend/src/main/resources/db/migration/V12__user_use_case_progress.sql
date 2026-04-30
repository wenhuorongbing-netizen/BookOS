CREATE TABLE user_use_case_progress (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    user_id BIGINT NOT NULL,
    use_case_slug VARCHAR(120) NOT NULL,
    status VARCHAR(32) NOT NULL,
    current_step INT NOT NULL DEFAULT 0,
    completed_step_keys TEXT,
    started_at DATETIME(6) DEFAULT NULL,
    completed_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_use_case_progress_user_slug (user_id, use_case_slug),
    KEY idx_use_case_progress_user_status (user_id, status),
    CONSTRAINT fk_use_case_progress_user FOREIGN KEY (user_id) REFERENCES users (id)
);
