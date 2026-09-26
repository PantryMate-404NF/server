CREATE TABLE IF NOT EXISTS ai_events (
    event_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(20) NOT NULL,
    recipe_id BIGINT NOT NULL,
    request_id VARCHAR(64),
    position INT,
    occurred_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'PENDING',
    attempts INT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS ix_ai_events_pending ON ai_events (event_id) WHERE status = 'PENDING';
