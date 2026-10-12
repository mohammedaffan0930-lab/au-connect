package edu.anurag.auconnect.web;

import edu.anurag.auconnect.dto.Dtos.HelpItemDto;
import edu.anurag.auconnect.model.HelpItem;
import edu.anurag.auconnect.model.Role;
import edu.anurag.auconnect.model.User;
import edu.anurag.auconnect.repo.HelpItemRepository;
import edu.anurag.auconnect.service.ApiException;
import edu.anurag.auconnect.service.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/** Help desk: lost and found item reports. */
@RestController
@RequestMapping("/api/helpdesk")
public class HelpDeskController {

    private final HelpItemRepository items;
    private final FileStorageService files;

    public HelpDeskController(HelpItemRepository items, FileStorageService files) {
        this.items = items;
        this.files = files;
    }

    @GetMapping
    public List<HelpItemDto> list(@RequestParam(required = false) HelpItem.ItemType type,
                                  @RequestParam(required = false) HelpItem.Status status) {
        return items.findAllByOrderByCreatedAtDesc().stream()
                .filter(h -> type == null || h.getType() == type)
                .filter(h -> status == null || h.getStatus() == status)
                .map(HelpItemDto::of)
                .toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public HelpItemDto create(@AuthenticationPrincipal User me,
                              @RequestParam HelpItem.ItemType type,
                              @RequestParam String title,
                              @RequestParam(required = false) String description,
                              @RequestParam(required = false) String location,
                              @RequestParam(required = false) MultipartFile photo) {
        String t = title == null ? "" : title.trim();
        if (t.isEmpty() || t.length() > 150) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Title must be between 1 and 150 characters");
        }
        if (description != null && description.length() > 1000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Description is too long (max 1000 characters)");
        }
        HelpItem h = new HelpItem();
        h.setReporter(me);
        h.setType(type);
        h.setTitle(t);
        h.setDescription(blankToNull(description));
        h.setLocation(blankToNull(location));
        h.setPhoto(files.storeImage(photo));
        return HelpItemDto.of(items.save(h));
    }

    @PutMapping("/{id}/resolve")
    public HelpItemDto resolve(@PathVariable Long id, @AuthenticationPrincipal User me) {
        HelpItem h = find(id);
        requireOwnerOrAdmin(h, me);
        h.setStatus(HelpItem.Status.RESOLVED);
        return HelpItemDto.of(items.save(h));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User me) {
        HelpItem h = find(id);
        requireOwnerOrAdmin(h, me);
        items.delete(h);
    }

    private HelpItem find(Long id) {
        return items.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Report not found"));
    }

    private void requireOwnerOrAdmin(HelpItem h, User me) {
        if (!h.getReporter().getId().equals(me.getId()) && me.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only the reporter or an admin can do that");
        }
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
