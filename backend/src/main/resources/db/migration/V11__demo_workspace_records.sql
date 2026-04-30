CREATE TABLE demo_records (
    id BIGINT NOT NULL AUTO_INCREMENT,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    entity_id BIGINT NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    user_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_demo_records_user_entity (user_id, entity_type, entity_id),
    KEY idx_demo_records_user_type (user_id, entity_type),
    CONSTRAINT fk_demo_records_user FOREIGN KEY (user_id) REFERENCES users (id)
);
