package edu.anurag.auconnect.service;

import edu.anurag.auconnect.config.Catalog;
import edu.anurag.auconnect.dto.Dtos.*;
import edu.anurag.auconnect.model.Complaint;
import edu.anurag.auconnect.model.ComplaintVote;
import edu.anurag.auconnect.model.User;
import edu.anurag.auconnect.repo.ComplaintRepository;
import edu.anurag.auconnect.repo.ComplaintVoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ComplaintService {

    private final ComplaintRepository complaints;
    private final ComplaintVoteRepository votes;

    public ComplaintService(ComplaintRepository complaints, ComplaintVoteRepository votes) {
        this.complaints = complaints;
        this.votes = votes;
    }

    /** More student votes means higher priority. */
    public static String priorityFor(long votes) {
        if (votes >= 20) return "CRITICAL";
        if (votes >= 10) return "HIGH";
        if (votes >= 5) return "MEDIUM";
        return "LOW";
    }

    private Map<Long, Long> voteCounts() {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] row : votes.countsPerComplaint()) {
            map.put((Long) row[0], (Long) row[1]);
        }
        return map;
    }

    @Transactional
    public MyComplaintDto submit(User me, ComplaintRequest req) {
        if (!Catalog.isCategory(req.category())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please choose a valid category");
        }
        Complaint c = new Complaint();
        c.setAuthor(me);
        c.setTitle(req.title().trim());
        c.setDescription(req.description().trim());
        c.setCategory(req.category());
        complaints.save(c);
        return toMine(c, 0);
    }

    /** Student view: all complaints, anonymised, so students can vote on them. */
    @Transactional(readOnly = true)
    public List<PublicComplaintDto> publicList(User me) {
        Map<Long, Long> counts = voteCounts();
        Set<Long> mine = new HashSet<>(votes.complaintIdsVotedBy(me.getId()));
        return complaints.findAllByOrderByCreatedAtDesc().stream()
                .map(c -> {
                    long n = counts.getOrDefault(c.getId(), 0L);
                    return new PublicComplaintDto(c.getId(), c.getTitle(), c.getCategory(), c.getStatus(), n,
                            priorityFor(n), mine.contains(c.getId()), c.getAuthor().getId().equals(me.getId()),
                            c.getCreatedAt());
                })
                .sorted(Comparator.comparingLong(PublicComplaintDto::votes).reversed()
                        .thenComparing(PublicComplaintDto::createdAt, Comparator.reverseOrder()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MyComplaintDto> mine(User me) {
        Map<Long, Long> counts = voteCounts();
        return complaints.findByAuthorIdOrderByCreatedAtDesc(me.getId()).stream()
                .map(c -> toMine(c, counts.getOrDefault(c.getId(), 0L)))
                .toList();
    }

    @Transactional
    public VoteResponse toggleVote(User me, Long complaintId) {
        Complaint c = complaints.findById(complaintId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Complaint not found"));
        if (c.getAuthor().getId().equals(me.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot vote on your own complaint");
        }
        var existing = votes.findByComplaintIdAndUserId(complaintId, me.getId());
        boolean voted;
        if (existing.isPresent()) {
            votes.delete(existing.get());
            voted = false;
        } else {
            ComplaintVote v = new ComplaintVote();
            v.setComplaint(c);
            v.setUser(me);
            votes.save(v);
            voted = true;
        }
        votes.flush();
        long n = votes.countByComplaintId(complaintId);
        return new VoteResponse(voted, n, priorityFor(n));
    }

    // ---------- admin ----------

    @Transactional(readOnly = true)
    public List<AdminComplaintDto> adminList() {
        Map<Long, Long> counts = voteCounts();
        return complaints.findAll().stream()
                .map(c -> toAdmin(c, counts.getOrDefault(c.getId(), 0L)))
                .sorted(Comparator.comparingLong(AdminComplaintDto::votes).reversed()
                        .thenComparing(AdminComplaintDto::createdAt, Comparator.reverseOrder()))
                .toList();
    }

    @Transactional
    public AdminComplaintDto adminUpdate(Long id, AdminComplaintUpdate req) {
        Complaint c = complaints.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Complaint not found"));
        c.setStatus(req.status());
        c.setAdminNote(req.adminNote() == null || req.adminNote().isBlank() ? null : req.adminNote().trim());
        c.setUpdatedAt(Instant.now());
        complaints.save(c);
        return toAdmin(c, votes.countByComplaintId(id));
    }

    // ---------- mapping ----------

    private MyComplaintDto toMine(Complaint c, long n) {
        return new MyComplaintDto(c.getId(), c.getTitle(), c.getDescription(), c.getCategory(), c.getStatus(),
                c.getAdminNote(), n, priorityFor(n), c.getCreatedAt(), c.getUpdatedAt());
    }

    private AdminComplaintDto toAdmin(Complaint c, long n) {
        User a = c.getAuthor();
        return new AdminComplaintDto(c.getId(), c.getTitle(), c.getDescription(), c.getCategory(), c.getStatus(),
                c.getAdminNote(), n, priorityFor(n), c.getCreatedAt(), c.getUpdatedAt(),
                a.getId(), a.getFullName(), a.getEmail(), a.getDepartment());
    }
}
