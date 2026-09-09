ALTER TABLE analysis_results
    ADD COLUMN observation_from VARCHAR(40) NOT NULL DEFAULT '',
    ADD COLUMN observation_to VARCHAR(40) NOT NULL DEFAULT '',
    ADD COLUMN commit_sample_capped BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN observation_limitations TEXT NOT NULL DEFAULT '[]',
    ADD COLUMN classification_status VARCHAR(30) NOT NULL DEFAULT 'CLASSIFIED',
    ADD COLUMN classification_margin INTEGER NOT NULL DEFAULT 0;
