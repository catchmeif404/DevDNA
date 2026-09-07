ALTER TABLE analysis_results
    DROP COLUMN night_owl_score,
    DROP COLUMN bug_slayer_score,
    DROP COLUMN builder_score,
    DROP COLUMN polyglot_score,
    ADD COLUMN type_scores TEXT;
