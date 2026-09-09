CREATE TABLE site_visit_events (
    id BIGSERIAL PRIMARY KEY,
    hostname VARCHAR(255) NOT NULL,
    visitor_id VARCHAR(64),
    visited_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_site_visit_events_visited_at ON site_visit_events (visited_at);
CREATE INDEX idx_site_visit_events_visitor_id ON site_visit_events (visitor_id);
