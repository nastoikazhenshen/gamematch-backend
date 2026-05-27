package kz.gamematch.service.cabinet;

import kz.gamematch.dto.cabinet.MatchHistoryDto;
import kz.gamematch.dto.cabinet.MyResponseDto;
import kz.gamematch.dto.team.PlayerReviewResponseDto;
import kz.gamematch.dto.team.TeamMemberDto;
import kz.gamematch.entity.*;
import kz.gamematch.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerCabinetService {

    private final UserRepository userRepository;
    private final RequestResponseRepository requestResponseRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final PlayerProfileRepository playerProfileRepository;
    private final PlayerReviewRepository playerReviewRepository;

    @Transactional(readOnly = true)
    public List<MyResponseDto> getMyResponses(String email, ResponseStatus status) {
        Long userId = userIdByEmail(email);

        List<RequestResponse> responses = status == null
                ? requestResponseRepository.findByResponderIdOrderByCreatedAtDesc(userId)
                : requestResponseRepository.findByResponderIdAndStatusOrderByCreatedAtDesc(userId, status);

        return responses.stream().map(this::mapResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<MatchHistoryDto> getMatchHistory(String email) {
        Long userId = userIdByEmail(email);

        return teamMemberRepository.findByUserId(userId)
                .stream()
                .map(TeamMember::getTeam)
                .filter(team -> team.getCompletedAt() != null)
                .sorted(Comparator.comparing(Team::getCompletedAt).reversed())
                .map(team -> mapHistory(team, userId))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlayerReviewResponseDto> getReceivedReviews(String email) {
        Long userId = userIdByEmail(email);

        return playerReviewRepository.findByReviewedUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapReview)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PlayerReviewResponseDto> getGivenReviews(String email) {
        Long userId = userIdByEmail(email);

        return playerReviewRepository.findByReviewerIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapReview)
                .toList();
    }

    private Long userIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private MyResponseDto mapResponse(RequestResponse response) {
        TeammateRequest request = response.getRequest();
        User author = request.getAuthor();

        return new MyResponseDto(
                response.getId(),
                request.getId(),
                request.getTitle(),
                request.getGame().getName(),
                author.getId(),
                nickname(author.getId()),
                response.getMessage(),
                response.getStatus(),
                response.getCreatedAt(),
                request.getDesiredPlayTime()
        );
    }

    private MatchHistoryDto mapHistory(Team team, Long currentUserId) {
        List<TeamMemberDto> teammates = teamMemberRepository.findByTeamId(team.getId())
                .stream()
                .filter(member -> !member.getUser().getId().equals(currentUserId))
                .map(member -> new TeamMemberDto(member.getUser().getId(), nickname(member.getUser().getId())))
                .toList();

        return new MatchHistoryDto(
                team.getId(),
                team.getRequest().getId(),
                team.getRequest().getTitle(),
                team.getGame().getName(),
                teammates,
                team.getCreatedAt(),
                team.getCompletedAt(),
                team.getCompletedBy() == null ? null : team.getCompletedBy().getId()
        );
    }

    private PlayerReviewResponseDto mapReview(PlayerReview review) {
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
}
