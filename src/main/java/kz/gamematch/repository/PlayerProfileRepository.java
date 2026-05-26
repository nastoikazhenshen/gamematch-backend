package kz.gamematch.repository;

import kz.gamematch.entity.PlayerProfile;
import kz.gamematch.dto.profile.SuggestedPlayerResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlayerProfileRepository extends JpaRepository<PlayerProfile, Long> {

    Optional<PlayerProfile> findByNickname(String nickname);

    Optional<PlayerProfile> findByUserId(Long userId);

    boolean existsByNickname(String nickname);

    @Query("""
            select new kz.gamematch.dto.profile.SuggestedPlayerResponseDto(
                profile.id,
                profile.user.id,
                profile.nickname,
                profile.karma,
                count(request.id)
            )
            from PlayerProfile profile
            left join TeammateRequest request on request.author.id = profile.user.id
            where (:excludedUserId is null or profile.user.id <> :excludedUserId)
            group by profile.id, profile.user.id, profile.nickname, profile.karma
            order by count(request.id) desc, profile.karma desc, profile.nickname asc
            """)
    List<SuggestedPlayerResponseDto> findSuggestedPlayers(Long excludedUserId, Pageable pageable);
}
