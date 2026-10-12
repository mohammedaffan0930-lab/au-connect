package edu.anurag.auconnect.dto;

import edu.anurag.auconnect.model.*;
import jakarta.validation.constraints.*;

import java.time.Instant;

/** All request/response shapes in one place. */
public final class Dtos {
    private Dtos() {}

    // ---------- helpers ----------
    public static String photoUrl(String filename) {
        return filename == null ? null : "/uploads/" + filename;
    }

    // ---------- auth ----------
    public record RegisterRequest(
            @NotBlank @Size(max = 100) String fullName,
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8, max = 100) String password,
            @NotBlank String department,
            @NotNull Role role,
            @Min(1) @Max(6) Integer yearOfStudy) {}

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}

    public record ResendRequest(@NotBlank @Email String email) {}

    public record MessageResponse(String message, String devVerificationLink) {
        public static MessageResponse of(String message) {
            return new MessageResponse(message, null);
        }
    }

    public record AuthResponse(String token, UserDto user) {}

    // ---------- users ----------
    public record UserDto(Long id, String email, String fullName, String department, Integer yearOfStudy,
                          String bio, String photoUrl, Role role, Instant createdAt) {
        public static UserDto of(User u, boolean includeEmail) {
            return new UserDto(u.getId(), includeEmail ? u.getEmail() : null, u.getFullName(),
                    u.getDepartment(), u.getYearOfStudy(), u.getBio(), Dtos.photoUrl(u.getProfilePhoto()),
                    u.getRole(), u.getCreatedAt());
        }
    }

    public record ProfileUpdate(
            @NotBlank @Size(max = 100) String fullName,
            @NotBlank String department,
            @Min(1) @Max(6) Integer yearOfStudy,
            @Size(max = 500) String bio) {}

    // ---------- posts ----------
    public record PostDto(Long id, String content, String department, PostType type, String photoUrl,
                          Instant createdAt, Long authorId, String authorName, String authorPhotoUrl,
                          Role authorRole) {
        public static PostDto of(Post p) {
            User a = p.getAuthor();
                return new PostDto(p.getId(), p.getContent(), p.getDepartment(), p.getType(), Dtos.photoUrl(p.getPhoto()),
                    p.getCreatedAt(), a.getId(), a.getFullName(), Dtos.photoUrl(a.getProfilePhoto()), a.getRole());
        }
    }

    // ---------- help desk ----------
    public record HelpItemDto(Long id, HelpItem.ItemType type, String title, String description, String location,
                              String photoUrl, HelpItem.Status status, Instant createdAt,
                              Long reporterId, String reporterName, String reporterEmail) {
        public static HelpItemDto of(HelpItem h) {
            User r = h.getReporter();
            return new HelpItemDto(h.getId(), h.getType(), h.getTitle(), h.getDescription(), h.getLocation(),
                    Dtos.photoUrl(h.getPhoto()), h.getStatus(), h.getCreatedAt(), r.getId(), r.getFullName(), r.getEmail());
        }
    }

    // ---------- complaints ----------
    public record ComplaintRequest(
            @NotBlank @Size(max = 200) String title,
            @NotBlank @Size(max = 3000) String description,
            @NotBlank String category) {}

    public record AdminComplaintUpdate(@NotNull Complaint.Status status, @Size(max = 1000) String adminNote) {}

    /** What other students see: no author, no description. */
    public record PublicComplaintDto(Long id, String title, String category, Complaint.Status status,
                                     long votes, String priority, boolean votedByMe, boolean mine,
                                     Instant createdAt) {}

    /** What the author sees about their own complaint. */
    public record MyComplaintDto(Long id, String title, String description, String category,
                                 Complaint.Status status, String adminNote, long votes, String priority,
                                 Instant createdAt, Instant updatedAt) {}

    /** Full details for admins. */
    public record AdminComplaintDto(Long id, String title, String description, String category,
                                    Complaint.Status status, String adminNote, long votes, String priority,
                                    Instant createdAt, Instant updatedAt,
                                    Long authorId, String authorName, String authorEmail, String authorDepartment) {}

    public record VoteResponse(boolean voted, long votes, String priority) {}

    // ---------- meta ----------
    public record Option(String code, String label) {}

    public record MetaResponse(java.util.List<Option> departments, java.util.List<Option> complaintCategories) {}

    public record StatsResponse(long users, long posts, long openHelpItems, long complaintsTotal,
                                long complaintsUnresolved) {}
}
