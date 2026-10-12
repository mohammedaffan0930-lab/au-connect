package edu.anurag.auconnect.web;

import edu.anurag.auconnect.config.Catalog;
import edu.anurag.auconnect.dto.Dtos.PostDto;
import edu.anurag.auconnect.model.Post;
import edu.anurag.auconnect.model.PostType;
import edu.anurag.auconnect.model.Role;
import edu.anurag.auconnect.model.User;
import edu.anurag.auconnect.repo.PostRepository;
import edu.anurag.auconnect.service.ApiException;
import edu.anurag.auconnect.service.FileStorageService;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository posts;
    private final FileStorageService files;

    public PostController(PostRepository posts, FileStorageService files) {
        this.posts = posts;
        this.files = files;
    }

    /**
     * Department feed. With a department, returns that department's posts plus university-wide (ALL) ones.
     * Without it, returns everything.
     */
    @GetMapping
    public List<PostDto> feed(@RequestParam(required = false) String department,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 50));
        var result = (department == null || department.isBlank() || "ALL".equals(department))
                ? posts.findAllByOrderByCreatedAtDesc(pageable)
                : posts.findByDepartmentInOrderByCreatedAtDesc(List.of(department, "ALL"), pageable);
        return result.getContent().stream().map(PostDto::of).toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public PostDto create(@AuthenticationPrincipal User me,
                          @RequestParam String content,
                          @RequestParam(defaultValue = "ALL") String department,
                          @RequestParam(defaultValue = "POST") PostType type,
                          @RequestParam(required = false) MultipartFile photo) {
        String text = content == null ? "" : content.trim();
        if (text.isEmpty() || text.length() > 2000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Post must be between 1 and 2000 characters");
        }
        if (!Catalog.isDepartment(department)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please choose a valid department");
        }
        if (type == PostType.NOTICE && me.getRole() == Role.STUDENT) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only faculty and admins can post official notices");
        }
        Post p = new Post();
        p.setAuthor(me);
        p.setContent(text);
        p.setDepartment(department);
        p.setType(type);
        p.setPhoto(files.storeImage(photo));
        return PostDto.of(posts.save(p));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id, @AuthenticationPrincipal User me) {
        Post p = posts.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Post not found"));
        if (!p.getAuthor().getId().equals(me.getId()) && me.getRole() != Role.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You can only delete your own posts");
        }
        posts.delete(p);
    }
}
