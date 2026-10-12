package edu.anurag.auconnect.config;

import edu.anurag.auconnect.model.*;
import edu.anurag.auconnect.repo.PostRepository;
import edu.anurag.auconnect.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the admin account (and a welcome notice) on first start. */
@Component
public class DataSeeder implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository users;
    private final PostRepository posts;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminPassword;

    public DataSeeder(UserRepository users, PostRepository posts, PasswordEncoder encoder,
                      @Value("${app.admin.email}") String adminEmail,
                      @Value("${app.admin.password}") String adminPassword) {
        this.users = users;
        this.posts = posts;
        this.encoder = encoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (users.existsByEmailIgnoreCase(adminEmail)) return;

        User admin = new User();
        admin.setEmail(adminEmail.toLowerCase());
        admin.setPasswordHash(encoder.encode(adminPassword));
        admin.setFullName("AU Connect Admin");
        admin.setDepartment("ALL");
        admin.setRole(Role.ADMIN);
        admin.setEmailVerified(true);
        admin.setBio("Official administrator account for AU Connect.");
        users.save(admin);

        Post welcome = new Post();
        welcome.setAuthor(admin);
        welcome.setDepartment("ALL");
        welcome.setType(PostType.NOTICE);
        welcome.setContent("Welcome to AU Connect! Share updates with your department, report lost and found items "
                + "on the Help Desk, and raise concerns privately through the Complaint Desk.");
        posts.save(welcome);

        log.info("Created default admin account: {} (change its password!)", adminEmail);
    }
}
