package edu.anurag.auconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A private complaint. The author and the full description are visible only to
 * the author and to admins; other students see just the title, category, votes and status.
 */
@Entity
@Table(name = "complaints")
@Getter
@Setter
@NoArgsConstructor
public class Complaint {

    public enum Status { SUBMITTED, UNDER_REVIEW, IN_PROGRESS, RESOLVED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private User author;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 3000)
    private String description;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.SUBMITTED;

    @Column(length = 1000)
    private String adminNote;

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();
}
