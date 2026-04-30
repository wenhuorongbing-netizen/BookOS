CREATE TABLE project_wizard_submissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    owner_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    client_step_intent VARCHAR(500) DEFAULT NULL,
    response_json LONGTEXT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_wizard_submission_idempotency (project_id, owner_id, idempotency_key),
    KEY idx_project_wizard_submissions_owner (owner_id),
    KEY idx_project_wizard_submissions_project (project_id),
    CONSTRAINT fk_project_wizard_submissions_owner FOREIGN KEY (owner_id) REFERENCES users (id),
    CONSTRAINT fk_project_wizard_submissions_project FOREIGN KEY (project_id) REFERENCES game_projects (id)
);
