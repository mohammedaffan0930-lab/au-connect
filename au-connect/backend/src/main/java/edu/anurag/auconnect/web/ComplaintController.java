package edu.anurag.auconnect.web;

import edu.anurag.auconnect.dto.Dtos.*;
import edu.anurag.auconnect.model.User;
import edu.anurag.auconnect.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Complaint desk, student side. Complaints are private: only admins see authors and full details. */
@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService service;

    public ComplaintController(ComplaintService service) {
        this.service = service;
    }

    @PostMapping
    public MyComplaintDto submit(@AuthenticationPrincipal User me, @Valid @RequestBody ComplaintRequest req) {
        return service.submit(me, req);
    }

    /** Anonymised list for voting. */
    @GetMapping
    public List<PublicComplaintDto> list(@AuthenticationPrincipal User me) {
        return service.publicList(me);
    }

    @GetMapping("/mine")
    public List<MyComplaintDto> mine(@AuthenticationPrincipal User me) {
        return service.mine(me);
    }

    @PostMapping("/{id}/vote")
    public VoteResponse vote(@PathVariable Long id, @AuthenticationPrincipal User me) {
        return service.toggleVote(me, id);
    }
}
