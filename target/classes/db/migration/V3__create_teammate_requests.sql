CREATE TABLE teammate_requests (
                                   id BIGSERIAL PRIMARY KEY,
                                   author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                   game_id BIGINT NOT NULL REFERENCES games(id),
                                   title VARCHAR(120) NOT NULL,
                                   description TEXT,
                                   required_role VARCHAR(80),
                                   min_rank VARCHAR(80),
                                   max_rank VARCHAR(80),
                                   desired_play_time TIMESTAMP,
                                   status VARCHAR(30) NOT NULL DEFAULT 'ACTIVE',
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE request_responses (
                                   id BIGSERIAL PRIMARY KEY,
                                   request_id BIGINT NOT NULL REFERENCES teammate_requests(id) ON DELETE CASCADE,
                                   responder_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                   message TEXT,
                                   status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                                   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   UNIQUE(request_id, responder_id)
);

CREATE INDEX idx_teammate_requests_game ON teammate_requests(game_id);
CREATE INDEX idx_teammate_requests_status ON teammate_requests(status);
CREATE INDEX idx_teammate_requests_author ON teammate_requests(author_id);