package kz.gamematch.service.profile;

import kz.gamematch.dto.profile.ProfileResponseDto;
import kz.gamematch.dto.profile.UpdateProfileRequestDto;
import kz.gamematch.dto.profile.PlayerGameResponseDto;
import kz.gamematch.dto.profile.PlayerStatsResponseDto;
import kz.gamematch.dto.profile.SuggestedPlayerResponseDto;
import kz.gamematch.dto.profile.UpsertPlayerGameRequestDto;
import kz.gamematch.entity.Game;
import kz.gamematch.entity.GameRank;
import kz.gamematch.entity.PlayerGame;
import kz.gamematch.entity.PlayerProfile;
import kz.gamematch.entity.ResponseStatus;
import kz.gamematch.entity.User;
import kz.gamematch.repository.GameRankRepository;
import kz.gamematch.repository.GameRepository;
import kz.gamematch.repository.PlayerGameRepository;
import kz.gamematch.repository.PlayerProfileRepository;
import kz.gamematch.repository.RequestResponseRepository;
import kz.gamematch.repository.TeamMemberRepository;
import kz.gamematch.repository.TeammateRequestRepository;
import kz.gamematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final PlayerProfileRepository playerProfileRepository;
    private final UserRepository userRepository;
    private final PlayerGameRepository playerGameRepository;
    private final GameRepository gameRepository;
    private final GameRankRepository gameRankRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final RequestResponseRepository requestResponseRepository;
    private final TeammateRequestRepository teammateRequestRepository;

    public ProfileResponseDto getProfileByUserId(Long userId) {
        PlayerProfile profile = playerProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));

        return mapToDto(profile);
    }

    public ProfileResponseDto getProfileById(Long profileId) {
        PlayerProfile profile = playerProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return mapToDto(profile);
    }

    public ProfileResponseDto searchByNickname(String nickname) {
        PlayerProfile profile = playerProfileRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return mapToDto(profile);
    }

    @Transactional(readOnly = true)
    public List<SuggestedPlayerResponseDto> getSuggestedPlayers(int limit) {
        return getSuggestedPlayers(null, limit);
    }

    @Transactional(readOnly = true)
    public List<SuggestedPlayerResponseDto> getSuggestedPlayers(Long excludedUserId, int limit) {
        int size = Math.max(1, Math.min(limit, 20));
        return playerProfileRepository.findSuggestedPlayers(excludedUserId, PageRequest.of(0, size));
    }

    public ProfileResponseDto updateProfile(Long userId, UpdateProfileRequestDto request) {
        PlayerProfile profile = playerProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));

        if (request.getNickname() != null && !request.getNickname().equals(profile.getNickname())) {
            if (playerProfileRepository.existsByNickname(request.getNickname())) {
                throw new RuntimeException("Nickname already exists");
            }
            profile.setNickname(request.getNickname());
        }

        if (request.getTimezone() != null) {
            profile.setTimezone(request.getTimezone());
        }

        if (request.getAveragePlayTime() != null) {
            profile.setAveragePlayTime(request.getAveragePlayTime());
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        PlayerProfile savedProfile = playerProfileRepository.save(profile);
        return mapToDto(savedProfile);
    }

    @Transactional(readOnly = true)
    public PlayerStatsResponseDto getStatsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }

        long playedMatches = teamMemberRepository.countByUserId(userId);
        long sentResponses = requestResponseRepository.countByResponderId(userId);
        long acceptedResponses = requestResponseRepository.countByResponderIdAndStatus(userId, ResponseStatus.ACCEPTED);
        long authoredRequests = teammateRequestRepository.countByAuthorId(userId);

        return new PlayerStatsResponseDto(
                userId,
                playedMatches,
                sentResponses,
                acceptedResponses,
                acceptanceRate(acceptedResponses, sentResponses),
                authoredRequests
        );
    }

    @Transactional(readOnly = true)
    public PlayerStatsResponseDto getStatsByProfileId(Long profileId) {
        PlayerProfile profile = playerProfileRepository.findById(profileId)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return getStatsByUserId(profile.getUser().getId());
    }

    @Transactional
    public List<PlayerGameResponseDto> getProfileGamesByUserId(Long userId) {
        PlayerProfile profile = playerProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));

        return getProfileGames(profile.getId());
    }

    @Transactional(readOnly = true)
    public List<PlayerGameResponseDto> getProfileGames(Long profileId) {
        if (!playerProfileRepository.existsById(profileId)) {
            throw new RuntimeException("Profile not found");
        }

        return playerGameRepository.findByProfileId(profileId)
                .stream()
                .map(this::mapPlayerGameToDto)
                .toList();
    }

    @Transactional
    public PlayerGameResponseDto addOrUpdatePlayerGame(Long userId, UpsertPlayerGameRequestDto request) {
        PlayerProfile profile = playerProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));
        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found"));

        validateRank(game.getId(), request.getRank());

        PlayerGame playerGame = playerGameRepository.findByProfileIdAndGameId(profile.getId(), game.getId())
                .orElseGet(() -> {
                    PlayerGame created = new PlayerGame();
                    created.setProfile(profile);
                    created.setGame(game);
                    return created;
                });

        playerGame.setRank(blankToNull(request.getRank()));
        playerGame.setMainRole(blankToNull(request.getMainRole()));

        return mapPlayerGameToDto(playerGameRepository.save(playerGame));
    }

    @Transactional
    public PlayerGameResponseDto updatePlayerGame(Long userId, Long playerGameId, UpsertPlayerGameRequestDto request) {
        PlayerProfile profile = playerProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));
        PlayerGame playerGame = playerGameRepository.findById(playerGameId)
                .orElseThrow(() -> new RuntimeException("Player game not found"));

        if (!playerGame.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Only profile owner can update player game");
        }

        validateRank(playerGame.getGame().getId(), request.getRank());
        playerGame.setRank(blankToNull(request.getRank()));
        playerGame.setMainRole(blankToNull(request.getMainRole()));

        return mapPlayerGameToDto(playerGameRepository.save(playerGame));
    }

    @Transactional
    public void deletePlayerGame(Long userId, Long playerGameId) {
        PlayerProfile profile = playerProfileRepository.findByUserId(userId)
                .orElseGet(() -> createDefaultProfile(userId));
        PlayerGame playerGame = playerGameRepository.findById(playerGameId)
                .orElseThrow(() -> new RuntimeException("Player game not found"));

        if (!playerGame.getProfile().getId().equals(profile.getId())) {
            throw new RuntimeException("Only profile owner can delete player game");
        }

        playerGameRepository.delete(playerGame);
    }

    private ProfileResponseDto mapToDto(PlayerProfile profile) {
        return new ProfileResponseDto(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getNickname(),
                profile.getTimezone(),
                profile.getAveragePlayTime(),
                profile.getBio(),
                profile.getKarma(),
                profile.getCompletedMatches()
        );
    }

    private PlayerGameResponseDto mapPlayerGameToDto(PlayerGame playerGame) {
        return new PlayerGameResponseDto(
                playerGame.getId(),
                playerGame.getGame().getId(),
                playerGame.getGame().getName(),
                playerGame.getRank(),
                rankImageUrl(playerGame),
                playerGame.getMainRole()
        );
    }

    private String rankImageUrl(PlayerGame playerGame) {
        if (playerGame.getRank() == null || playerGame.getRank().isBlank()) {
            return null;
        }

        return gameRankRepository.findByGameIdAndNameIgnoreCase(playerGame.getGame().getId(), playerGame.getRank())
                .map(GameRank::getImageUrl)
                .orElse(null);
    }

    private void validateRank(Long gameId, String rank) {
        if (rank == null || rank.isBlank()) {
            return;
        }

        gameRankRepository.findByGameIdAndNameIgnoreCase(gameId, rank.trim())
                .orElseThrow(() -> new RuntimeException("Rank does not belong to selected game"));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private int acceptanceRate(long acceptedResponses, long sentResponses) {
        if (sentResponses == 0) {
            return 0;
        }

        return (int) Math.round((acceptedResponses * 100.0) / sentResponses);
    }

    private PlayerProfile createDefaultProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        PlayerProfile profile = new PlayerProfile();
        profile.setUser(user);
        profile.setNickname(defaultNickname(user));
        profile.setKarma(BigDecimal.ZERO);
        profile.setCompletedMatches(0);

        return playerProfileRepository.save(profile);
    }

    private String defaultNickname(User user) {
        String email = user.getEmail();
        String base = email == null || email.isBlank()
                ? "player"
                : email.substring(0, email.indexOf("@") > 0 ? email.indexOf("@") : email.length());

        String normalized = base.replaceAll("[^A-Za-z0-9_-]", "");
        if (normalized.length() < 3) {
            normalized = "player";
        }

        String candidate = normalized.length() > 70 ? normalized.substring(0, 70) : normalized;
        if (!playerProfileRepository.existsByNickname(candidate)) {
            return candidate;
        }

        String suffix = "-" + user.getId();
        int maxBaseLength = 80 - suffix.length();
        if (candidate.length() > maxBaseLength) {
            candidate = candidate.substring(0, maxBaseLength);
        }
        return candidate + suffix;
    }
}
