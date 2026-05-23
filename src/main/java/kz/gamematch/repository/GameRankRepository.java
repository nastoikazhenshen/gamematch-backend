package kz.gamematch.repository;

import kz.gamematch.entity.GameRank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface GameRankRepository extends JpaRepository<GameRank, Long> {

    List<GameRank> findByGameIdOrderBySortOrderAscNameAsc(Long gameId);

    Optional<GameRank> findByGameIdAndNameIgnoreCase(Long gameId, String name);

    @Query("select rank from GameRank rank join fetch rank.game game order by game.name asc, rank.sortOrder asc, rank.name asc")
    List<GameRank> findAllWithGameOrder();
}
