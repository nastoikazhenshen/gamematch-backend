package kz.gamematch.repository;

import kz.gamematch.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByTeamId(Long teamId);

    List<TeamMember> findByUserId(Long userId);

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
}
