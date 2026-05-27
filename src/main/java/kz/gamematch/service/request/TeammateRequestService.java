package kz.gamematch.service.request;

import kz.gamematch.dto.request.CreateTeammateRequestDto;
import kz.gamematch.dto.request.TeammateRequestResponseDto;
import kz.gamematch.entity.Game;
import kz.gamematch.entity.GameRank;
import kz.gamematch.entity.PlayerProfile;
import kz.gamematch.entity.RequestStatus;
import kz.gamematch.entity.TeammateRequest;
import kz.gamematch.entity.User;
import kz.gamematch.mapper.TeammateRequestMapper;
import kz.gamematch.repository.GameRepository;
import kz.gamematch.repository.GameRankRepository;
import kz.gamematch.repository.TeammateRequestRepository;
import kz.gamematch.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeammateRequestService {

    private final TeammateRequestRepository teammateRequestRepository;
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final GameRankRepository gameRankRepository;
    private final TeammateRequestMapper teammateRequestMapper;

    public TeammateRequestResponseDto createRequest(CreateTeammateRequestDto requestDto) {
        validateDesiredPlayTime(requestDto.getDesiredPlayTime());

        User author = userRepository.findById(requestDto.getAuthorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));

        Game game = gameRepository.findById(requestDto.getGameId())
                .orElseThrow(() -> new RuntimeException("Game not found"));
        validateRankRange(game.getId(), requestDto.getMinRank(), requestDto.getMaxRank());

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

        return teammateRequestMapper.toDto(savedRequest);
    }

    public List<TeammateRequestResponseDto> getAllActiveRequests() {
        return teammateRequestRepository.findByStatus(RequestStatus.ACTIVE)
                .stream()
                .map(teammateRequestMapper::toDto)
                .toList();
    }

    public List<TeammateRequestResponseDto> getRequestsByGame(Long gameId) {
        return teammateRequestRepository.findByGameIdAndStatus(gameId, RequestStatus.ACTIVE)
                .stream()
                .map(teammateRequestMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<TeammateRequestResponseDto> searchActiveRequests(
            Long gameId,
            String role,
            String minRank,
            String maxRank,
            LocalDateTime desiredFrom,
            LocalDateTime desiredTo,
            Pageable pageable
    ) {
        return teammateRequestRepository.findAll(
                        activeRequestsSpecification(gameId, role, minRank, maxRank, desiredFrom, desiredTo),
                        pageable
                )
                .map(teammateRequestMapper::toDto);
    }

    public List<TeammateRequestResponseDto> getMyRequests(Long authorId) {
        return teammateRequestRepository.findByAuthorId(authorId)
                .stream()
                .map(teammateRequestMapper::toDto)
                .toList();
    }

    public TeammateRequestResponseDto getRequestById(Long requestId) {
        TeammateRequest request = teammateRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        return teammateRequestMapper.toDto(request);
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

    private Specification<TeammateRequest> activeRequestsSpecification(
            Long gameId,
            String role,
            String minRank,
            String maxRank,
            LocalDateTime desiredFrom,
            LocalDateTime desiredTo
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.equal(root.get("status"), RequestStatus.ACTIVE));

            if (gameId != null) {
                predicates.add(criteriaBuilder.equal(root.get("game").get("id"), gameId));
            }

            if (hasText(role)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("requiredRole")),
                        role.trim().toLowerCase()
                ));
            }

            if (hasText(minRank)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("minRank")),
                        minRank.trim().toLowerCase()
                ));
            }

            if (hasText(maxRank)) {
                predicates.add(criteriaBuilder.equal(
                        criteriaBuilder.lower(root.get("maxRank")),
                        maxRank.trim().toLowerCase()
                ));
            }

            if (desiredFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("desiredPlayTime"), desiredFrom));
            }

            if (desiredTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("desiredPlayTime"), desiredTo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private void validateDesiredPlayTime(LocalDateTime desiredPlayTime) {
        if (desiredPlayTime != null && desiredPlayTime.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Desired play time cannot be in the past");
        }
    }

    private void validateRankRange(Long gameId, String minRank, String maxRank) {
        if (!hasText(minRank) || !hasText(maxRank)) {
            return;
        }

        GameRank min = gameRankRepository.findByGameIdAndNameIgnoreCase(gameId, minRank.trim())
                .orElseThrow(() -> new RuntimeException("Minimum rank does not belong to selected game"));
        GameRank max = gameRankRepository.findByGameIdAndNameIgnoreCase(gameId, maxRank.trim())
                .orElseThrow(() -> new RuntimeException("Maximum rank does not belong to selected game"));

        if (min.getSortOrder() > max.getSortOrder()) {
            throw new RuntimeException("Maximum rank cannot be lower than minimum rank");
        }
    }
}
