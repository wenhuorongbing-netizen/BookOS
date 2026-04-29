ALTER TABLE forum_threads MODIFY COLUMN status ENUM('ACTIVE','ARCHIVED','CLOSED','HIDDEN','LOCKED','OPEN') NOT NULL DEFAULT 'OPEN';
UPDATE forum_threads SET status = 'OPEN' WHERE status = 'ACTIVE';
UPDATE forum_threads SET status = 'LOCKED' WHERE status = 'CLOSED';

ALTER TABLE forum_reports ADD COLUMN status ENUM('OPEN','RESOLVED') NOT NULL DEFAULT 'OPEN';
UPDATE forum_reports SET status = 'RESOLVED' WHERE resolved = TRUE;
