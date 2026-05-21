package kz.gamematch.repository;

import kz.gamematch.entity.PlayerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Long> {

    Optional<PlayerProfile> findByNickname(String nickname);

    Optional<PlayerProfile> findByUserId(Long userId);

    boolean existsByNickname(String nickname);
}
