package kz.gamematch.service.request;

import kz.gamematch.dto.request.CreateTeammateRequestDto;
import kz.gamematch.dto.request.TeammateRequestResponseDto;
import kz.gamematch.entity.Game;
import kz.gamematch.entity.PlayerProfile;
import kz.gamematch.entity.RequestStatus;
import kz.gamematch.entity.TeammateRequest;
import kz.gamematch.entity.User;
import kz.gamematch.repository.GameRepository;
import kz.gamematch.repository.PlayerProfileRepository;
import kz.gamematch.repository.TeammateRequestRepository;
import kz.gamematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeammateRequestService {

    private final TeammateRequestRepository teammateRequestRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final PlayerProfileRepository playerProfileRepository;

    public TeammateRequestResponseDto createRequest(CreateTeammateRequestDto requestDto) {
        User author = userRepository.findById(requestDto.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Game game = gameRepository.findById(requestDto.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found"));

        TeammateRequest request = new TeammateRequest();
        request.setAuthor(author);
        request.setGame(game);
        request.setTitle(requestDto.getTitle());
        request.setDescription(requestDto.getDescription());
        request.setRequiredRole(requestDto.getRequiredRole());
        request.setMinRank(requestDto.getMinRank());
        request.setMaxRank(requestDto.getMaxRank());
        request.setDesiredPlayTime(requestDto.getDesiredPlayTime());
        request.setStatus(RequestStatus.ACTIVE);
        request.setCreatedAt(LocalDateTime.now());

        TeammateRequest savedRequest = teammateRequestRepository.save(request);

        return mapToDto(savedRequest);
    }

    public List<TeammateRequestResponseDto> getAllActiveRequests() {
        return teammateRequestRepository.findByStatus(RequestStatus.ACTIVE)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<TeammateRequestResponseDto> getRequestsByGame(Long gameId) {
        return teammateRequestRepository.findByGameIdAndStatus(gameId, RequestStatus.ACTIVE)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public List<TeammateRequestResponseDto> getMyRequests(Long authorId) {
        return teammateRequestRepository.findByAuthorId(authorId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public TeammateRequestResponseDto getRequestById(Long requestId) {
        TeammateRequest request = teammateRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        return mapToDto(request);
    }

    public void cancelRequest(Long requestId, Long authorId) {
        TeammateRequest request = teammateRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (!request.getAuthor().getId().equals(authorId)) {
            throw new RuntimeException("Only author can cancel request");
        }

        request.setStatus(RequestStatus.CANCELLED);
        teammateRequestRepository.save(request);
    }

    private TeammateRequestResponseDto mapToDto(TeammateRequest request) {
        PlayerProfile profile = playerProfileRepository.findByUserId(request.getAuthor().getId())
                .orElseThrow(() -> new RuntimeException("Author profile not found"));

        return new TeammateRequestResponseDto(
                request.getId(),
                request.getAuthor().getId(),
                profile.getNickname(),
                request.getGame().getId(),
                request.getGame().getName(),
                request.getTitle(),
                request.getDescription(),
                request.getRequiredRole(),
                request.getMinRank(),
                request.getMaxRank(),
                request.getDesiredPlayTime(),
                request.getStatus(),
                request.getCreatedAt()
        );
    }
}
