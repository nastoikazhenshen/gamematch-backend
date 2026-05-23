CREATE TABLE teams (
                       id BIGSERIAL PRIMARY KEY,
                       request_id BIGINT NOT NULL UNIQUE REFERENCES teammate_requests(id) ON DELETE CASCADE,
                       accepted_response_id BIGINT NOT NULL UNIQUE REFERENCES request_responses(id) ON DELETE CASCADE,
                       game_id BIGINT NOT NULL REFERENCES games(id),
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE team_members (
                              id BIGSERIAL PRIMARY KEY,
                              team_id BIGINT NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                              user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              UNIQUE(team_id, user_id)
);

CREATE TABLE chat_messages (
                               id BIGSERIAL PRIMARY KEY,
                               team_id BIGINT NOT NULL REFERENCES teams(id) ON DELETE CASCADE,
                               sender_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                               content TEXT NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_teams_request ON teams(request_id);
CREATE INDEX idx_team_members_user ON team_members(user_id);
CREATE INDEX idx_chat_messages_team_created ON chat_messages(team_id, created_at);
