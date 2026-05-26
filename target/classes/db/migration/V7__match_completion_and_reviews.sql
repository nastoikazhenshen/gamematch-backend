ALTER TABLE teams
    ADD COLUMN completed_at TIMESTAMP,
    ADD COLUMN completed_by_user_id BIGINT REFERENCES users(id);

CREATE TABLE player_reviews (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
    reviewer_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    reviewed_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    stars INT NOT NULL CHECK (stars BETWEEN 1 AND 5),
    comment TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(team_id, reviewer_id, reviewed_user_id),
    CHECK (reviewer_id <> reviewed_user_id)
);

CREATE INDEX idx_player_reviews_reviewed_user ON player_reviews(reviewed_user_id);
CREATE INDEX idx_player_reviews_team ON player_reviews(team_id);
CREATE INDEX idx_teams_completed_at ON teams(completed_at);
