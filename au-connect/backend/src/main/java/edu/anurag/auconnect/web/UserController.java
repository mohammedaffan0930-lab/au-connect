package edu.anurag.auconnect.web;

import edu.anurag.auconnect.config.Catalog;
import edu.anurag.auconnect.dto.Dtos.*;
import edu.anurag.auconnect.model.Role;
import edu.anurag.auconnect.model.User;
import edu.anurag.auconnect.repo.UserRepository;
import edu.anurag.auconnect.service.ApiException;
import edu.anurag.auconnect.service.FileStorageService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository users;
    private final FileStorageService files;

    public UserController(UserRepository users, FileStorageService files) {
        this.users = users;
        this.files = files;
    }

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal User me) {
        return UserDto.of(fresh(me), true);
    }

    @PutMapping("/me")
    public UserDto update(@AuthenticationPrincipal User me, @Valid @RequestBody ProfileUpdate req) {
        if (!Catalog.isDepartment(req.department())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please choose a valid department");
        }
        User u = fresh(me);
        u.setFullName(req.fullName().trim());
        u.setDepartment(req.department());
        u.setYearOfStudy(u.getRole() == Role.STUDENT ? req.yearOfStudy() : null);
        u.setBio(req.bio() == null || req.bio().isBlank() ? null : req.bio().trim());
        return UserDto.of(users.save(u), true);
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserDto uploadPhoto(@AuthenticationPrincipal User me, @RequestParam("photo") MultipartFile photo) {
        String stored = files.storeImage(photo);
        if (stored == null) throw new ApiException(HttpStatus.BAD_REQUEST, "Please choose an image");
        User u = fresh(me);
        u.setProfilePhoto(stored);
        return UserDto.of(users.save(u), true);
    }

    /** Another member's public profile (email hidden). */
    @GetMapping("/{id}")
    public UserDto profile(@PathVariable Long id, @AuthenticationPrincipal User me) {
        User u = users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
        return UserDto.of(u, u.getId().equals(me.getId()));
    }

    private User fresh(User principal) {
        return users.findById(principal.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
