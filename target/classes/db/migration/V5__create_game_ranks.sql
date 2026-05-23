CREATE TABLE game_ranks (
                            id BIGSERIAL PRIMARY KEY,
                            game_id BIGINT NOT NULL REFERENCES games(id) ON DELETE CASCADE,
                            name VARCHAR(80) NOT NULL,
                            image_url VARCHAR(255),
                            sort_order INT NOT NULL DEFAULT 0,
                            UNIQUE(game_id, name)
);

INSERT INTO games (name) VALUES ('PUBG')
ON CONFLICT (name) DO NOTHING;

INSERT INTO game_ranks (game_id, name, image_url, sort_order)
SELECT g.id, rank_name, image_url, sort_order
FROM games g
JOIN (
    VALUES
        ('Dota 2', 'Herald', '/img/ranks/dota.svg', 10),
        ('Dota 2', 'Guardian', '/img/ranks/dota.svg', 20),
        ('Dota 2', 'Crusader', '/img/ranks/dota.svg', 30),
        ('Dota 2', 'Archon', '/img/ranks/dota.svg', 40),
        ('Dota 2', 'Legend', '/img/ranks/dota.svg', 50),
        ('Dota 2', 'Ancient', '/img/ranks/dota.svg', 60),
        ('Dota 2', 'Divine', '/img/ranks/dota.svg', 70),
        ('Dota 2', 'Immortal', '/img/ranks/dota.svg', 80),

        ('CS2', 'Silver I', '/img/ranks/cs2.svg', 10),
        ('CS2', 'Silver II', '/img/ranks/cs2.svg', 20),
        ('CS2', 'Silver III', '/img/ranks/cs2.svg', 30),
        ('CS2', 'Silver IV', '/img/ranks/cs2.svg', 40),
        ('CS2', 'Silver Elite', '/img/ranks/cs2.svg', 50),
        ('CS2', 'Silver Elite Master', '/img/ranks/cs2.svg', 60),
        ('CS2', 'Gold Nova I', '/img/ranks/cs2.svg', 70),
        ('CS2', 'Gold Nova II', '/img/ranks/cs2.svg', 80),
        ('CS2', 'Gold Nova III', '/img/ranks/cs2.svg', 90),
        ('CS2', 'Gold Nova Master', '/img/ranks/cs2.svg', 100),
        ('CS2', 'Master Guardian I', '/img/ranks/cs2.svg', 110),
        ('CS2', 'Master Guardian II', '/img/ranks/cs2.svg', 120),
        ('CS2', 'Master Guardian Elite', '/img/ranks/cs2.svg', 130),
        ('CS2', 'Distinguished Master Guardian', '/img/ranks/cs2.svg', 140),
        ('CS2', 'Legendary Eagle', '/img/ranks/cs2.svg', 150),
        ('CS2', 'Legendary Eagle Master', '/img/ranks/cs2.svg', 160),
        ('CS2', 'Supreme Master First Class', '/img/ranks/cs2.svg', 170),
        ('CS2', 'Global Elite', '/img/ranks/cs2.svg', 180),

        ('Valorant', 'Iron', '/img/ranks/valorant.svg', 10),
        ('Valorant', 'Bronze', '/img/ranks/valorant.svg', 20),
        ('Valorant', 'Silver', '/img/ranks/valorant.svg', 30),
        ('Valorant', 'Gold', '/img/ranks/valorant.svg', 40),
        ('Valorant', 'Platinum', '/img/ranks/valorant.svg', 50),
        ('Valorant', 'Diamond', '/img/ranks/valorant.svg', 60),
        ('Valorant', 'Ascendant', '/img/ranks/valorant.svg', 70),
        ('Valorant', 'Immortal', '/img/ranks/valorant.svg', 80),
        ('Valorant', 'Radiant', '/img/ranks/valorant.svg', 90),

        ('PUBG', 'Bronze', '/img/ranks/pubg.svg', 10),
        ('PUBG', 'Silver', '/img/ranks/pubg.svg', 20),
        ('PUBG', 'Gold', '/img/ranks/pubg.svg', 30),
        ('PUBG', 'Platinum', '/img/ranks/pubg.svg', 40),
        ('PUBG', 'Diamond', '/img/ranks/pubg.svg', 50),
        ('PUBG', 'Master', '/img/ranks/pubg.svg', 60),
        ('PUBG', 'Grandmaster', '/img/ranks/pubg.svg', 70),
        ('PUBG', 'Conqueror', '/img/ranks/pubg.svg', 80)
) AS ranks(game_name, rank_name, image_url, sort_order)
ON ranks.game_name = g.name
ON CONFLICT (game_id, name) DO NOTHING;
