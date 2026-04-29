ALTER TABLE forum_threads
    ADD COLUMN related_project_id BIGINT DEFAULT NULL;

CREATE INDEX idx_forum_threads_related_project
    ON forum_threads (related_project_id);

ALTER TABLE forum_threads
    ADD CONSTRAINT fk_forum_threads_related_project
    FOREIGN KEY (related_project_id) REFERENCES game_projects (id);
