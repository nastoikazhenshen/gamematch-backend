package kz.gamematch.service.request;

import kz.gamematch.dto.response.CreateResponseDto;
import kz.gamematch.dto.response.RequestResponseDto;
import kz.gamematch.entity.*;
import kz.gamematch.repository.PlayerProfileRepository;
import kz.gamematch.repository.RequestResponseRepository;
import kz.gamematch.repository.TeammateRequestRepository;
import kz.gamematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestResponseService {

    private final RequestResponseRepository requestResponseRepository;
    private final TeammateRequestRepository teammateRequestRepository;
    private final UserRepository userRepository;
    private final PlayerProfileRepository playerProfileRepository;

    public RequestResponseDto createResponse(Long requestId, CreateResponseDto dto) {
        TeammateRequest request = teammateRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != RequestStatus.ACTIVE) {
            throw new RuntimeException("Request is not active");
        }

        User responder = userRepository.findById(dto.getResponderId())
                .orElseThrow(() -> new RuntimeException("Responder not found"));

        if (request.getAuthor().getId().equals(responder.getId())) {
            throw new RuntimeException("Author cannot respond to own request");
        }

        if (requestResponseRepository.existsByRequestIdAndResponderId(requestId, responder.getId())) {
            throw new RuntimeException("You already responded to this request");
        }

        RequestResponse response = new RequestResponse();
        response.setRequest(request);
        response.setResponder(responder);
        response.setMessage(dto.getMessage());
        response.setStatus(ResponseStatus.PENDING);
        response.setCreatedAt(LocalDateTime.now());

        RequestResponse savedResponse = requestResponseRepository.save(response);

        return mapToDto(savedResponse);
    }

    public List<RequestResponseDto> getResponsesByRequest(Long requestId) {
        return requestResponseRepository.findByRequestId(requestId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public RequestResponseDto acceptResponse(Long responseId, Long authorId) {
        RequestResponse response = requestResponseRepository.findById(responseId)
                .orElseThrow(() -> new RuntimeException("Response not found"));

        TeammateRequest request = response.getRequest();

        if (!request.getAuthor().getId().equals(authorId)) {
            throw new RuntimeException("Only request author can accept response");
        }

        if (request.getStatus() != RequestStatus.ACTIVE) {
            throw new RuntimeException("Request is not active");
        }

        response.setStatus(ResponseStatus.ACCEPTED);
        request.setStatus(RequestStatus.CLOSED);

        teammateRequestRepository.save(request);
        RequestResponse savedResponse = requestResponseRepository.save(response);

        return mapToDto(savedResponse);
    }

    public RequestResponseDto rejectResponse(Long responseId, Long authorId) {
        RequestResponse response = requestResponseRepository.findById(responseId)
                .orElseThrow(() -> new RuntimeException("Response not found"));

        TeammateRequest request = response.getRequest();

        if (!request.getAuthor().getId().equals(authorId)) {
            throw new RuntimeException("Only request author can reject response");
        }

        response.setStatus(ResponseStatus.REJECTED);

        RequestResponse savedResponse = requestResponseRepository.save(response);

        return mapToDto(savedResponse);
    }

    private RequestResponseDto mapToDto(RequestResponse response) {
        PlayerProfile responderProfile = playerProfileRepository
                .findByUserId(response.getResponder().getId())
                .orElseThrow(() -> new RuntimeException("Responder profile not found"));

        return new RequestResponseDto(
                response.getId(),
                response.getRequest().getId(),
                response.getResponder().getId(),
                responderProfile.getNickname(),
                response.getMessage(),
                response.getStatus(),
                response.getCreatedAt()
        );
    }
}