CREATE TABLE game_projects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    archived_at DATETIME(6) DEFAULT NULL,
    description LONGTEXT DEFAULT NULL,
    genre VARCHAR(120) DEFAULT NULL,
    platform VARCHAR(120) DEFAULT NULL,
    progress_percent INT NOT NULL,
    slug VARCHAR(260) NOT NULL,
    stage VARCHAR(64) NOT NULL,
    title VARCHAR(220) NOT NULL,
    visibility ENUM('PRIVATE','PUBLIC','SHARED') NOT NULL,
    owner_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_game_projects_owner_slug (owner_id, slug),
    KEY idx_game_projects_owner_archived (owner_id, archived_at),
    CONSTRAINT fk_game_projects_owner FOREIGN KEY (owner_id) REFERENCES users (id)
);

CREATE TABLE project_problems (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    description LONGTEXT DEFAULT NULL,
    priority VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,
    title VARCHAR(220) NOT NULL,
    project_id BIGINT NOT NULL,
    related_source_reference_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_project_problems_project_status (project_id, status),
    KEY idx_project_problems_source (related_source_reference_id),
    CONSTRAINT fk_project_problems_project FOREIGN KEY (project_id) REFERENCES game_projects (id),
    CONSTRAINT fk_project_problems_source FOREIGN KEY (related_source_reference_id) REFERENCES source_references (id)
);

CREATE TABLE project_applications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    application_type VARCHAR(64) NOT NULL,
    description LONGTEXT DEFAULT NULL,
    source_entity_id BIGINT DEFAULT NULL,
    source_entity_type VARCHAR(64) DEFAULT NULL,
    status VARCHAR(64) NOT NULL,
    title VARCHAR(220) NOT NULL,
    project_id BIGINT NOT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_project_applications_project_status (project_id, status),
    KEY idx_project_applications_source (source_entity_type, source_entity_id),
    KEY idx_project_applications_source_ref (source_reference_id),
    CONSTRAINT fk_project_applications_project FOREIGN KEY (project_id) REFERENCES game_projects (id),
    CONSTRAINT fk_project_applications_source_ref FOREIGN KEY (source_reference_id) REFERENCES source_references (id)
);

CREATE TABLE design_decisions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    decision LONGTEXT NOT NULL,
    rationale LONGTEXT DEFAULT NULL,
    status VARCHAR(64) NOT NULL,
    title VARCHAR(220) NOT NULL,
    tradeoffs LONGTEXT DEFAULT NULL,
    project_id BIGINT NOT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_design_decisions_project_status (project_id, status),
    KEY idx_design_decisions_source (source_reference_id),
    CONSTRAINT fk_design_decisions_project FOREIGN KEY (project_id) REFERENCES game_projects (id),
    CONSTRAINT fk_design_decisions_source FOREIGN KEY (source_reference_id) REFERENCES source_references (id)
);

CREATE TABLE playtest_plans (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    hypothesis LONGTEXT DEFAULT NULL,
    status VARCHAR(64) NOT NULL,
    success_criteria LONGTEXT DEFAULT NULL,
    target_players LONGTEXT DEFAULT NULL,
    tasks LONGTEXT DEFAULT NULL,
    title VARCHAR(220) NOT NULL,
    project_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_playtest_plans_project_status (project_id, status),
    CONSTRAINT fk_playtest_plans_project FOREIGN KEY (project_id) REFERENCES game_projects (id)
);

CREATE TABLE playtest_sessions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    notes LONGTEXT DEFAULT NULL,
    participant_label VARCHAR(180) DEFAULT NULL,
    session_date DATETIME(6) DEFAULT NULL,
    plan_id BIGINT NOT NULL,
    project_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_playtest_sessions_project (project_id),
    KEY idx_playtest_sessions_plan (plan_id),
    CONSTRAINT fk_playtest_sessions_plan FOREIGN KEY (plan_id) REFERENCES playtest_plans (id),
    CONSTRAINT fk_playtest_sessions_project FOREIGN KEY (project_id) REFERENCES game_projects (id)
);

CREATE TABLE playtest_findings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    observation LONGTEXT DEFAULT NULL,
    recommendation LONGTEXT DEFAULT NULL,
    severity VARCHAR(64) NOT NULL,
    status VARCHAR(64) NOT NULL,
    title VARCHAR(220) NOT NULL,
    project_id BIGINT NOT NULL,
    session_id BIGINT DEFAULT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_playtest_findings_project_status (project_id, status),
    KEY idx_playtest_findings_session (session_id),
    KEY idx_playtest_findings_source (source_reference_id),
    CONSTRAINT fk_playtest_findings_project FOREIGN KEY (project_id) REFERENCES game_projects (id),
    CONSTRAINT fk_playtest_findings_session FOREIGN KEY (session_id) REFERENCES playtest_sessions (id),
    CONSTRAINT fk_playtest_findings_source FOREIGN KEY (source_reference_id) REFERENCES source_references (id)
);

CREATE TABLE iteration_logs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    changes_made LONGTEXT DEFAULT NULL,
    evidence LONGTEXT DEFAULT NULL,
    summary LONGTEXT DEFAULT NULL,
    title VARCHAR(220) NOT NULL,
    project_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    KEY idx_iteration_logs_project (project_id),
    CONSTRAINT fk_iteration_logs_project FOREIGN KEY (project_id) REFERENCES game_projects (id)
);

CREATE TABLE project_knowledge_links (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    note LONGTEXT DEFAULT NULL,
    relationship_type VARCHAR(64) NOT NULL,
    target_id BIGINT NOT NULL,
    target_type VARCHAR(64) NOT NULL,
    project_id BIGINT NOT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_project_knowledge_link (project_id, target_type, target_id, relationship_type),
    KEY idx_project_knowledge_links_project (project_id),
    KEY idx_project_knowledge_links_source (source_reference_id),
    CONSTRAINT fk_project_knowledge_links_project FOREIGN KEY (project_id) REFERENCES game_projects (id),
    CONSTRAINT fk_project_knowledge_links_source FOREIGN KEY (source_reference_id) REFERENCES source_references (id)
);

CREATE TABLE project_lens_reviews (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    answer LONGTEXT DEFAULT NULL,
    question VARCHAR(500) NOT NULL,
    score INT DEFAULT NULL,
    status VARCHAR(64) NOT NULL,
    knowledge_object_id BIGINT DEFAULT NULL,
    project_id BIGINT NOT NULL,
    source_reference_id BIGINT DEFAULT NULL,
    PRIMARY KEY (id),
    KEY idx_project_lens_reviews_project_status (project_id, status),
    KEY idx_project_lens_reviews_knowledge (knowledge_object_id),
    KEY idx_project_lens_reviews_source (source_reference_id),
    CONSTRAINT fk_project_lens_reviews_knowledge FOREIGN KEY (knowledge_object_id) REFERENCES knowledge_objects (id),
    CONSTRAINT fk_project_lens_reviews_project FOREIGN KEY (project_id) REFERENCES game_projects (id),
    CONSTRAINT fk_project_lens_reviews_source FOREIGN KEY (source_reference_id) REFERENCES source_references (id)
);
