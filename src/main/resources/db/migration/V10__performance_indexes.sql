CREATE INDEX IF NOT EXISTS idx_teammate_requests_status_game_time
    ON teammate_requests(status, game_id, desired_play_time);

CREATE INDEX IF NOT EXISTS idx_teammate_requests_status_role
    ON teammate_requests(status, required_role);

CREATE INDEX IF NOT EXISTS idx_request_responses_responder_status_created
    ON request_responses(responder_id, status, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_player_profiles_karma_matches_nickname
    ON player_profiles(karma DESC, completed_matches DESC, nickname);

CREATE INDEX IF NOT EXISTS idx_player_reviews_reviewer_created
    ON player_reviews(reviewer_id, created_at DESC);
