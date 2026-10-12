package edu.anurag.auconnect.repo;

import edu.anurag.auconnect.model.ComplaintVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComplaintVoteRepository extends JpaRepository<ComplaintVote, Long> {

    Optional<ComplaintVote> findByComplaintIdAndUserId(Long complaintId, Long userId);

    long countByComplaintId(Long complaintId);

    /** Rows of [complaintId, voteCount]. */
    @Query("select v.complaint.id, count(v) from ComplaintVote v group by v.complaint.id")
    List<Object[]> countsPerComplaint();

    @Query("select v.complaint.id from ComplaintVote v where v.user.id = :userId")
    List<Long> complaintIdsVotedBy(@Param("userId") Long userId);
}
