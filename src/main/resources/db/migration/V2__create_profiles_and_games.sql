CREATE TABLE player_profiles (
                                 id BIGSERIAL PRIMARY KEY,
                                 user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
                                 nickname VARCHAR(80) NOT NULL UNIQUE,
                                 timezone VARCHAR(50),
                                 average_play_time VARCHAR(100),
                                 bio TEXT,
                                 karma NUMERIC(3,2) NOT NULL DEFAULT 0.00,
                                 completed_matches INT NOT NULL DEFAULT 0
);

CREATE TABLE games (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE player_games (
                              id BIGSERIAL PRIMARY KEY,
                              profile_id BIGINT NOT NULL REFERENCES player_profiles(id) ON DELETE CASCADE,
                              game_id BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
                              rank VARCHAR(80),
                              main_role VARCHAR(80),
                              UNIQUE(profile_id, game_id)
);

INSERT INTO games (name) VALUES
                             ('Dota 2'),
                             ('CS2'),
                             ('Valorant');