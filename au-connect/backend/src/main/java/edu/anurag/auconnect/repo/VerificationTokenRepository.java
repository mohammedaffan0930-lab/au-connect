package edu.anurag.auconnect.repo;

import edu.anurag.auconnect.model.User;
import edu.anurag.auconnect.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    void deleteByUser(User user);
}
