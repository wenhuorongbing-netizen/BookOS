ALTER TABLE entity_links ADD COLUMN note LONGTEXT DEFAULT NULL;
ALTER TABLE entity_links ADD COLUMN created_by VARCHAR(32) NOT NULL DEFAULT 'SYSTEM';

CREATE INDEX idx_entity_links_created_by ON entity_links (user_id, created_by);
