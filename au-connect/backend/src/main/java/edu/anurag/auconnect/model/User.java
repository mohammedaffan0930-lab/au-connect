package edu.anurag.auconnect.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String fullName;

    private String department;

    private Integer yearOfStudy;

    @Column(length = 500)
    private String bio;

    private String profilePhoto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private boolean emailVerified;

    private Instant createdAt = Instant.now();
}
