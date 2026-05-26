ALTER TABLE teams
    DROP CONSTRAINT IF EXISTS teams_completed_by_user_id_fkey;

ALTER TABLE teams
    ADD CONSTRAINT teams_completed_by_user_id_fkey
    FOREIGN KEY (completed_by_user_id) REFERENCES users(id) ON DELETE SET NULL;
