package kz.gamematch.repository;

import kz.gamematch.entity.RequestStatus;
import kz.gamematch.entity.TeammateRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeammateRequestRepository extends JpaRepository<TeammateRequest, Long> {

    List<TeammateRequest> findByStatus(RequestStatus status);

    List<TeammateRequest> findByGameIdAndStatus(Long gameId, RequestStatus status);

    List<TeammateRequest> findByAuthorId(Long authorId);
}
