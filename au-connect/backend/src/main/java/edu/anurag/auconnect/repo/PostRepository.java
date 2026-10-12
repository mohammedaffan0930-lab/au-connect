package edu.anurag.auconnect.repo;

import edu.anurag.auconnect.model.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Post> findByDepartmentInOrderByCreatedAtDesc(Collection<String> departments, Pageable pageable);
}
