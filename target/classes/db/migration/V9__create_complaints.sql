CREATE TABLE complaints (
    id BIGSERIAL PRIMARY KEY,
    reporter_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reported_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reason TEXT NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMP,
    resolved_by_user_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    CHECK (reporter_id <> reported_user_id)
);

CREATE INDEX idx_complaints_status ON complaints(status);
CREATE INDEX idx_complaints_reported_user ON complaints(reported_user_id);
CREATE INDEX idx_complaints_created_at ON complaints(created_at);
