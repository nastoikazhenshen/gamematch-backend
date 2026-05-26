package kz.gamematch.repository;

import kz.gamematch.entity.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {

    List<TeamMember> findByTeamId(Long teamId);

    List<TeamMember> findByUserId(Long userId);

    long countByUserId(Long userId);

    @Query("select count(member) from TeamMember member where member.user.id = :userId and member.team.completedAt is not null")
    long countCompletedTeamsByUserId(Long userId);

    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
}
