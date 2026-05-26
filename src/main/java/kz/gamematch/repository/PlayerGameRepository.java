package kz.gamematch.repository;

import kz.gamematch.entity.PlayerGame;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerGameRepository extends JpaRepository<PlayerGame, Long> {

    List<PlayerGame> findByProfileId(Long profileId);

    List<PlayerGame> findByGameId(Long gameId);

    Optional<PlayerGame> findByProfileIdAndGameId(Long profileId, Long gameId);
}
