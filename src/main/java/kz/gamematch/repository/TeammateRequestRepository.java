package kz.gamematch.repository;

import kz.gamematch.entity.RequestStatus;
import kz.gamematch.entity.TeammateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface TeammateRequestRepository extends JpaRepository<TeammateRequest, Long>,
        JpaSpecificationExecutor<TeammateRequest> {

    List<TeammateRequest> findByStatus(RequestStatus status);

    List<TeammateRequest> findByGameIdAndStatus(Long gameId, RequestStatus status);

    List<TeammateRequest> findByAuthorId(Long authorId);
}
