package kz.gamematch.repository;

import kz.gamematch.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findByRequestId(Long requestId);

    Optional<Team> findByAcceptedResponseId(Long acceptedResponseId);
}
