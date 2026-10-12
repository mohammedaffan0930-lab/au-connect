package edu.anurag.auconnect.repo;

import edu.anurag.auconnect.model.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {
    List<Complaint> findAllByOrderByCreatedAtDesc();

    List<Complaint> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
