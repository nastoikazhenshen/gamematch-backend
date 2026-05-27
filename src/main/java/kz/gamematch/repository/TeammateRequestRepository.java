package kz.gamematch.repository;

import kz.gamematch.entity.RequestStatus;
import kz.gamematch.entity.TeammateRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface TeammateRequestRepository extends JpaRepository<TeammateRequest, Long>,
        JpaSpecificationExecutor<TeammateRequest> {

    List<TeammateRequest> findByStatus(RequestStatus status);

    List<TeammateRequest> findByGameIdAndStatus(Long gameId, RequestStatus status);

    List<TeammateRequest> findByAuthorId(Long authorId);

    long countByAuthorId(Long authorId);

    long countByStatus(RequestStatus status);

    long countByStatusNotAndCreatedAtBefore(RequestStatus status, LocalDateTime cutoff);

    @Modifying
    @Query("delete from TeammateRequest request where request.status <> :status and request.createdAt < :cutoff")
    int deleteByStatusNotAndCreatedAtBefore(RequestStatus status, LocalDateTime cutoff);
}
