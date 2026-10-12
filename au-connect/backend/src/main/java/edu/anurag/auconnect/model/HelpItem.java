package edu.anurag.auconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** A lost or found item report on the help desk. */
@Entity
@Table(name = "help_items")
@Getter
@Setter
@NoArgsConstructor
public class HelpItem {

    public enum ItemType { LOST, FOUND }

    public enum Status { OPEN, RESOLVED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    private User reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ItemType type;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 1000)
    private String description;

    private String location;

    private String photo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.OPEN;

    private Instant createdAt = Instant.now();
}
