package edu.anurag.auconnect.web;

import edu.anurag.auconnect.dto.Dtos.*;
import edu.anurag.auconnect.model.Complaint;
import edu.anurag.auconnect.model.HelpItem;
import edu.anurag.auconnect.repo.ComplaintRepository;
import edu.anurag.auconnect.repo.HelpItemRepository;
import edu.anurag.auconnect.repo.PostRepository;
import edu.anurag.auconnect.repo.UserRepository;
import edu.anurag.auconnect.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Everything under /api/admin requires the ADMIN role (enforced in SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ComplaintService complaints;
    private final ComplaintRepository complaintRepo;
    private final UserRepository users;
    private final PostRepository posts;
    private final HelpItemRepository helpItems;

    public AdminController(ComplaintService complaints, ComplaintRepository complaintRepo, UserRepository users,
                           PostRepository posts, HelpItemRepository helpItems) {
        this.complaints = complaints;
        this.complaintRepo = complaintRepo;
        this.users = users;
        this.posts = posts;
        this.helpItems = helpItems;
    }

    @GetMapping("/complaints")
    public List<AdminComplaintDto> complaints() {
        return complaints.adminList();
    }

    @PutMapping("/complaints/{id}")
    public AdminComplaintDto update(@PathVariable Long id, @Valid @RequestBody AdminComplaintUpdate req) {
        return complaints.adminUpdate(id, req);
    }

    @GetMapping("/stats")
    public StatsResponse stats() {
        long open = helpItems.findAll().stream().filter(h -> h.getStatus() == HelpItem.Status.OPEN).count();
        long all = complaintRepo.count();
        long unresolved = complaintRepo.findAll().stream()
                .filter(c -> c.getStatus() != Complaint.Status.RESOLVED).count();
        return new StatsResponse(users.count(), posts.count(), open, all, unresolved);
    }
}
