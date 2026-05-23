package kz.gamematch.service.team;

import kz.gamematch.dto.team.TeamMemberDto;
import kz.gamematch.dto.team.TeamResponseDto;
import kz.gamematch.entity.PlayerProfile;
import kz.gamematch.entity.Team;
import kz.gamematch.entity.TeamMember;
import kz.gamematch.repository.PlayerProfileRepository;
import kz.gamematch.repository.TeamMemberRepository;
import kz.gamematch.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PlayerProfileRepository playerProfileRepository;

    @Transactional(readOnly = true)
    public TeamResponseDto getTeamById(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        return mapToDto(team);
    }

    @Transactional(readOnly = true)
    public TeamResponseDto getTeamByRequestId(Long requestId) {
        Team team = teamRepository.findByRequestId(requestId)
                .orElseThrow(() -> new RuntimeException("Team not found"));

        return mapToDto(team);
    }

    @Transactional(readOnly = true)
    public List<TeamResponseDto> getTeamsByUserId(Long userId) {
        return teamMemberRepository.findByUserId(userId)
                .stream()
                .map(TeamMember::getTeam)
                .map(this::mapToDto)
                .toList();
    }

    private TeamResponseDto mapToDto(Team team) {
        List<TeamMemberDto> members = teamMemberRepository.findByTeamId(team.getId())
                .stream()
                .map(this::mapMemberToDto)
                .toList();

        return new TeamResponseDto(
                team.getId(),
                team.getRequest().getId(),
                team.getAcceptedResponse().getId(),
                team.getGame().getId(),
                team.getGame().getName(),
                members,
                team.getCreatedAt()
        );
    }

    private TeamMemberDto mapMemberToDto(TeamMember member) {
        PlayerProfile profile = playerProfileRepository.findByUserId(member.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Member profile not found"));

        return new TeamMemberDto(member.getUser().getId(), profile.getNickname());
    }
}
