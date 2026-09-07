CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    github_id BIGINT NOT NULL UNIQUE,
    github_login VARCHAR(255) NOT NULL,
    avatar_url VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE analysis_jobs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    github_username VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    progress INTEGER NOT NULL DEFAULT 0,
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_analysis_jobs_username ON analysis_jobs (github_username);

CREATE TABLE analysis_results (
    id BIGSERIAL PRIMARY KEY,
    analysis_job_id BIGINT NOT NULL REFERENCES analysis_jobs (id),
    github_username VARCHAR(255) NOT NULL,
    total_commits INTEGER NOT NULL DEFAULT 0,
    total_repositories INTEGER NOT NULL DEFAULT 0,
    total_pull_requests INTEGER NOT NULL DEFAULT 0,
    peak_hour INTEGER,
    peak_weekday INTEGER,
    top_language VARCHAR(100),
    language_ratios TEXT,
    night_owl_score INTEGER NOT NULL DEFAULT 0,
    bug_slayer_score INTEGER NOT NULL DEFAULT 0,
    builder_score INTEGER NOT NULL DEFAULT 0,
    polyglot_score INTEGER NOT NULL DEFAULT 0,
    developer_type VARCHAR(50) NOT NULL,
    dna_vector TEXT,
    ai_summary TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_analysis_results_username ON analysis_results (github_username);
