UPDATE game_ranks rank
SET image_url = badges.image_url
FROM games game
JOIN (
    VALUES
        ('Dota 2', 'Herald', '/rank-badges/dota-2/herald.svg'),
        ('Dota 2', 'Guardian', '/rank-badges/dota-2/guardian.svg'),
        ('Dota 2', 'Crusader', '/rank-badges/dota-2/crusader.svg'),
        ('Dota 2', 'Archon', '/rank-badges/dota-2/archon.svg'),
        ('Dota 2', 'Legend', '/rank-badges/dota-2/legend.svg'),
        ('Dota 2', 'Ancient', '/rank-badges/dota-2/ancient.svg'),
        ('Dota 2', 'Divine', '/rank-badges/dota-2/divine.svg'),
        ('Dota 2', 'Immortal', '/rank-badges/dota-2/immortal.svg'),

        ('CS2', 'Silver I', '/rank-badges/cs2/silver-i.svg'),
        ('CS2', 'Silver II', '/rank-badges/cs2/silver-ii.svg'),
        ('CS2', 'Silver III', '/rank-badges/cs2/silver-iii.svg'),
        ('CS2', 'Silver IV', '/rank-badges/cs2/silver-iv.svg'),
        ('CS2', 'Silver Elite', '/rank-badges/cs2/silver-elite.svg'),
        ('CS2', 'Silver Elite Master', '/rank-badges/cs2/silver-elite-master.svg'),
        ('CS2', 'Gold Nova I', '/rank-badges/cs2/gold-nova-i.svg'),
        ('CS2', 'Gold Nova II', '/rank-badges/cs2/gold-nova-ii.svg'),
        ('CS2', 'Gold Nova III', '/rank-badges/cs2/gold-nova-iii.svg'),
        ('CS2', 'Gold Nova Master', '/rank-badges/cs2/gold-nova-master.svg'),
        ('CS2', 'Master Guardian I', '/rank-badges/cs2/master-guardian-i.svg'),
        ('CS2', 'Master Guardian II', '/rank-badges/cs2/master-guardian-ii.svg'),
        ('CS2', 'Master Guardian Elite', '/rank-badges/cs2/master-guardian-elite.svg'),
        ('CS2', 'Distinguished Master Guardian', '/rank-badges/cs2/distinguished-master-guardian.svg'),
        ('CS2', 'Legendary Eagle', '/rank-badges/cs2/legendary-eagle.svg'),
        ('CS2', 'Legendary Eagle Master', '/rank-badges/cs2/legendary-eagle-master.svg'),
        ('CS2', 'Supreme Master First Class', '/rank-badges/cs2/supreme-master-first-class.svg'),
        ('CS2', 'Global Elite', '/rank-badges/cs2/global-elite.svg'),

        ('Valorant', 'Iron', '/rank-badges/valorant/iron.svg'),
        ('Valorant', 'Bronze', '/rank-badges/valorant/bronze.svg'),
        ('Valorant', 'Silver', '/rank-badges/valorant/silver.svg'),
        ('Valorant', 'Gold', '/rank-badges/valorant/gold.svg'),
        ('Valorant', 'Platinum', '/rank-badges/valorant/platinum.svg'),
        ('Valorant', 'Diamond', '/rank-badges/valorant/diamond.svg'),
        ('Valorant', 'Ascendant', '/rank-badges/valorant/ascendant.svg'),
        ('Valorant', 'Immortal', '/rank-badges/valorant/immortal.svg'),
        ('Valorant', 'Radiant', '/rank-badges/valorant/radiant.svg'),

        ('PUBG', 'Bronze', '/rank-badges/pubg/bronze.svg'),
        ('PUBG', 'Silver', '/rank-badges/pubg/silver.svg'),
        ('PUBG', 'Gold', '/rank-badges/pubg/gold.svg'),
        ('PUBG', 'Platinum', '/rank-badges/pubg/platinum.svg'),
        ('PUBG', 'Diamond', '/rank-badges/pubg/diamond.svg'),
        ('PUBG', 'Master', '/rank-badges/pubg/master.svg'),
        ('PUBG', 'Grandmaster', '/rank-badges/pubg/grandmaster.svg'),
        ('PUBG', 'Conqueror', '/rank-badges/pubg/conqueror.svg')
) AS badges(game_name, rank_name, image_url)
ON badges.game_name = game.name
WHERE rank.game_id = game.id
  AND rank.name = badges.rank_name;
