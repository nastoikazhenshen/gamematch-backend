package kz.gamematch.repository;

import kz.gamematch.entity.Complaint;
import kz.gamematch.entity.ComplaintStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    List<Complaint> findByStatusOrderByCreatedAtDesc(ComplaintStatus status);

    List<Complaint> findAllByOrderByCreatedAtDesc();

    long countByStatus(ComplaintStatus status);
}
