package edu.anurag.auconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private User author;

    /** Department code, or ALL for university-wide posts. */
    @Column(nullable = false)
    private String department;

    @Column(nullable = false, length = 2000)
    private String content;

    private String photo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type = PostType.POST;

    private Instant createdAt = Instant.now();
}
