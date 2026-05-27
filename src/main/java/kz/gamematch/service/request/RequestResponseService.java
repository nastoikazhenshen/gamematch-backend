package kz.gamematch.service.request;

import kz.gamematch.dto.response.CreateResponseDto;
import kz.gamematch.dto.response.RequestResponseDto;
import kz.gamematch.entity.*;
import kz.gamematch.mapper.RequestResponseMapper;
import kz.gamematch.repository.RequestResponseRepository;
import kz.gamematch.repository.TeamMemberRepository;
import kz.gamematch.repository.TeamRepository;
import kz.gamematch.repository.TeammateRequestRepository;
import kz.gamematch.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestResponseService {

    private final RequestResponseRepository requestResponseRepository;
    private final TeammateRequestRepository teammateRequestRepository;
    private final UserRepository userRepository;
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final RequestResponseMapper requestResponseMapper;

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

        return requestResponseMapper.toDto(savedResponse);
    }

    public List<RequestResponseDto> getResponsesByRequest(Long requestId) {
        return requestResponseRepository.findByRequestId(requestId)
                .stream()
                .map(requestResponseMapper::toDto)
                .toList();
    }

    @Transactional
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
        rejectOtherPendingResponses(request.getId(), savedResponse.getId());
        createTeamForAcceptedResponse(request, savedResponse);

        return requestResponseMapper.toDto(savedResponse);
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

        return requestResponseMapper.toDto(savedResponse);
    }

    private void rejectOtherPendingResponses(Long requestId, Long acceptedResponseId) {
        List<RequestResponse> pendingResponses = requestResponseRepository.findByRequestIdAndStatus(
                requestId,
                ResponseStatus.PENDING
        );

        pendingResponses.stream()
                .filter(response -> !response.getId().equals(acceptedResponseId))
                .forEach(response -> response.setStatus(ResponseStatus.REJECTED));

        requestResponseRepository.saveAll(pendingResponses);
    }

    private void createTeamForAcceptedResponse(TeammateRequest request, RequestResponse acceptedResponse) {
        Team team = new Team();
        team.setRequest(request);
        team.setAcceptedResponse(acceptedResponse);
        team.setGame(request.getGame());
        team.setCreatedAt(LocalDateTime.now());

        Team savedTeam = teamRepository.save(team);

        teamMemberRepository.save(createTeamMember(savedTeam, request.getAuthor()));
        teamMemberRepository.save(createTeamMember(savedTeam, acceptedResponse.getResponder()));
    }

    private TeamMember createTeamMember(Team team, User user) {
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(user);
        member.setCreatedAt(LocalDateTime.now());

        return member;
    }
}
