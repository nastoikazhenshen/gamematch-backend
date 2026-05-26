package kz.gamematch.repository;

import kz.gamematch.entity.PlayerReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerReviewRepository extends JpaRepository<PlayerReview, Long> {

    List<PlayerReview> findByReviewedUserIdOrderByCreatedAtDesc(Long reviewedUserId);

    List<PlayerReview> findByTeamId(Long teamId);

    Optional<PlayerReview> findByTeamIdAndReviewerIdAndReviewedUserId(Long teamId, Long reviewerId, Long reviewedUserId);

    boolean existsByTeamIdAndReviewerIdAndReviewedUserId(Long teamId, Long reviewerId, Long reviewedUserId);
}
