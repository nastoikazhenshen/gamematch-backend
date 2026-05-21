package kz.gamematch.repository;

import kz.gamematch.entity.RequestResponse;
import kz.gamematch.entity.ResponseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RequestResponseRepository extends JpaRepository<RequestResponse, Long> {

    List<RequestResponse> findByRequestId(Long requestId);

    List<RequestResponse> findByResponderId(Long responderId);

    Optional<RequestResponse> findByRequestIdAndResponderId(Long requestId, Long responderId);

    boolean existsByRequestIdAndResponderId(Long requestId, Long responderId);

    List<RequestResponse> findByStatus(ResponseStatus status);
}
