package kz.gamematch.mapper;

import kz.gamematch.dto.team.PlayerReviewResponseDto;
import kz.gamematch.dto.team.TeamMemberDto;
import kz.gamematch.dto.team.TeamResponseDto;
import kz.gamematch.entity.PlayerReview;
import kz.gamematch.entity.Team;
import kz.gamematch.entity.TeamMember;
import kz.gamematch.repository.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class TeamMapper {

    private final TeamMemberRepository teamMemberRepository;
    private final PlayerNicknameResolver nicknameResolver;

    public TeamResponseDto toDto(Team team) {
        List<TeamMemberDto> members = teamMemberRepository.findByTeamId(team.getId())
                .stream()
                .map(this::memberToDto)
                .toList();

        return new TeamResponseDto(
                team.getId(),
                team.getRequest().getId(),
                team.getAcceptedResponse().getId(),
                team.getGame().getId(),
                team.getGame().getName(),
                members,
                team.getCreatedAt(),
                team.getCompletedAt(),
                team.getCompletedBy() == null ? null : team.getCompletedBy().getId()
        );
    }

    public PlayerReviewResponseDto reviewToDto(PlayerReview review) {
        return new PlayerReviewResponseDto(
                review.getId(),
                review.getTeam().getId(),
                review.getReviewer().getId(),
                nicknameResolver.nickname(review.getReviewer().getId()),
                review.getReviewedUser().getId(),
                nicknameResolver.nickname(review.getReviewedUser().getId()),
                review.getStars(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    private TeamMemberDto memberToDto(TeamMember member) {
        Long userId = member.getUser().getId();
        return new TeamMemberDto(userId, nicknameResolver.requiredNickname(userId, "Member profile not found"));
    }
}
