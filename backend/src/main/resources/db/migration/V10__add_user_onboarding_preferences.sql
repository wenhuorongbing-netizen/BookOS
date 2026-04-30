ALTER TABLE user_profiles ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE user_profiles ADD COLUMN primary_use_case VARCHAR(80) DEFAULT NULL;
ALTER TABLE user_profiles ADD COLUMN starting_mode VARCHAR(40) DEFAULT NULL;
ALTER TABLE user_profiles ADD COLUMN preferred_dashboard_mode VARCHAR(40) DEFAULT NULL;
