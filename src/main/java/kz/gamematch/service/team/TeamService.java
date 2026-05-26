package kz.gamematch.service.team;

import kz.gamematch.dto.team.CreatePlayerReviewRequestDto;
import kz.gamematch.dto.team.PlayerReviewResponseDto;
import kz.gamematch.dto.team.TeamMemberDto;
import kz.gamematch.dto.team.TeamResponseDto;
import kz.gamematch.entity.PlayerReview;
import kz.gamematch.entity.PlayerProfile;
import kz.gamematch.entity.Team;
import kz.gamematch.entity.TeamMember;
import kz.gamematch.entity.User;
import kz.gamematch.repository.PlayerReviewRepository;
import kz.gamematch.repository.PlayerProfileRepository;
import kz.gamematch.repository.TeamMemberRepository;
import kz.gamematch.repository.TeamRepository;
import kz.gamematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PlayerReviewRepository playerReviewRepository;
    private final UserRepository userRepository;

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

    @Transactional
    public TeamResponseDto completeMatch(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new RuntimeException("Only team members can complete match");
        }

        if (team.getCompletedAt() == null) {
            team.setCompletedAt(LocalDateTime.now());
            team.setCompletedBy(user);
        }

        Team savedTeam = teamRepository.save(team);
        updateCompletedMatchesForTeam(savedTeam.getId());
        return mapToDto(savedTeam);
    }

    @Transactional
    public PlayerReviewResponseDto reviewPlayer(Long teamId, CreatePlayerReviewRequestDto request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found"));
        User reviewer = userRepository.findById(request.getReviewerId())
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));
        User reviewedUser = userRepository.findById(request.getReviewedUserId())
                .orElseThrow(() -> new RuntimeException("Reviewed user not found"));

        if (team.getCompletedAt() == null) {
            throw new RuntimeException("Match must be completed before review");
        }
        if (reviewer.getId().equals(reviewedUser.getId())) {
            throw new RuntimeException("Cannot review yourself");
        }
        if (!teamMemberRepository.existsByTeamIdAndUserId(teamId, reviewer.getId())
                || !teamMemberRepository.existsByTeamIdAndUserId(teamId, reviewedUser.getId())) {
            throw new RuntimeException("Only team members can review each other");
        }

        PlayerReview review = playerReviewRepository
                .findByTeamIdAndReviewerIdAndReviewedUserId(teamId, reviewer.getId(), reviewedUser.getId())
                .orElseGet(() -> {
                    PlayerReview created = new PlayerReview();
                    created.setTeam(team);
                    created.setReviewer(reviewer);
                    created.setReviewedUser(reviewedUser);
                    created.setCreatedAt(LocalDateTime.now());
                    return created;
                });

        review.setStars(request.getStars());
        review.setComment(blankToNull(request.getComment()));

        PlayerReview savedReview = playerReviewRepository.save(review);
        recalculateKarma(reviewedUser.getId());
        return mapReviewToDto(savedReview);
    }

    @Transactional(readOnly = true)
    public List<PlayerReviewResponseDto> getTeamReviews(Long teamId) {
        return playerReviewRepository.findByTeamId(teamId)
                .stream()
                .map(this::mapReviewToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlayerReviewResponseDto> getReceivedReviews(Long userId) {
        return playerReviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapReviewToDto)
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
                team.getCreatedAt(),
                team.getCompletedAt(),
                team.getCompletedBy() == null ? null : team.getCompletedBy().getId()
        );
    }

    private TeamMemberDto mapMemberToDto(TeamMember member) {
        PlayerProfile profile = playerProfileRepository.findByUserId(member.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Member profile not found"));

        return new TeamMemberDto(member.getUser().getId(), profile.getNickname());
    }

    private PlayerReviewResponseDto mapReviewToDto(PlayerReview review) {
        return new PlayerReviewResponseDto(
                review.getId(),
                review.getTeam().getId(),
                review.getReviewer().getId(),
                nickname(review.getReviewer().getId()),
                review.getReviewedUser().getId(),
                nickname(review.getReviewedUser().getId()),
                review.getStars(),
                review.getComment(),
                review.getCreatedAt()
        );
    }

    private String nickname(Long userId) {
        return playerProfileRepository.findByUserId(userId)
                .map(PlayerProfile::getNickname)
                .orElse("Player " + userId);
    }

    private void recalculateKarma(Long userId) {
        PlayerProfile profile = playerProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Reviewed player profile not found"));
        List<PlayerReview> reviews = playerReviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(userId);

        BigDecimal karma = (reviews.isEmpty()
                ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(reviews.stream()
                        .mapToInt(PlayerReview::getStars)
                        .average()
                        .orElse(0.0))
                .setScale(2, RoundingMode.HALF_UP));

        profile.setKarma(karma);
        profile.setCompletedMatches((int) teamMemberRepository.countCompletedTeamsByUserId(userId));
        playerProfileRepository.save(profile);
    }

    private void updateCompletedMatchesForTeam(Long teamId) {
        teamMemberRepository.findByTeamId(teamId).forEach(member -> {
            PlayerProfile profile = playerProfileRepository.findByUserId(member.getUser().getId())
                    .orElseThrow(() -> new RuntimeException("Member profile not found"));
            profile.setCompletedMatches((int) teamMemberRepository.countCompletedTeamsByUserId(member.getUser().getId()));
            playerProfileRepository.save(profile);
        });
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
