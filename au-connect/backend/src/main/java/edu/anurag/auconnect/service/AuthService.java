package edu.anurag.auconnect.service;

import edu.anurag.auconnect.config.Catalog;
import edu.anurag.auconnect.config.JwtService;
import edu.anurag.auconnect.dto.Dtos.*;
import edu.anurag.auconnect.model.Role;
import edu.anurag.auconnect.model.User;
import edu.anurag.auconnect.model.VerificationToken;
import edu.anurag.auconnect.repo.UserRepository;
import edu.anurag.auconnect.repo.VerificationTokenRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository users;
    private final VerificationTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final EmailService email;
    private final String allowedDomain;
    private final String frontendUrl;
    private final boolean devMode;

    public AuthService(UserRepository users, VerificationTokenRepository tokens, PasswordEncoder encoder,
                       JwtService jwt, EmailService email,
                       @Value("${app.allowed-email-domain}") String allowedDomain,
                       @Value("${app.frontend-url}") String frontendUrl,
                       @Value("${app.dev-mode}") boolean devMode) {
        this.users = users;
        this.tokens = tokens;
        this.encoder = encoder;
        this.jwt = jwt;
        this.email = email;
        this.allowedDomain = allowedDomain.toLowerCase(Locale.ROOT);
        this.frontendUrl = frontendUrl;
        this.devMode = devMode;
    }

    @Transactional
    public MessageResponse register(RegisterRequest req) {
        String address = req.email().trim().toLowerCase(Locale.ROOT);
        if (!address.endsWith("@" + allowedDomain)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Please use your official @" + allowedDomain + " email address");
        }
        if (req.role() == Role.ADMIN) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Choose Student or Faculty");
        }
        if (!Catalog.isDepartment(req.department()) || "ALL".equals(req.department())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Please choose a valid department");
        }
        if (users.existsByEmailIgnoreCase(address)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "This email is already registered. Try logging in, or resend the verification email.");
        }

        User user = new User();
        user.setEmail(address);
        user.setPasswordHash(encoder.encode(req.password()));
        user.setFullName(req.fullName().trim());
        user.setDepartment(req.department());
        user.setRole(req.role());
        user.setYearOfStudy(req.role() == Role.STUDENT ? req.yearOfStudy() : null);
        user.setEmailVerified(false);
        users.save(user);

        return sendVerification(user);
    }

    @Transactional
    public MessageResponse resend(String rawEmail) {
        String address = rawEmail.trim().toLowerCase(Locale.ROOT);
        var found = users.findByEmailIgnoreCase(address).filter(u -> !u.isEmailVerified());
        // Same answer whether or not the account exists, so emails can't be probed.
        if (found.isEmpty()) {
            return MessageResponse.of("If that account exists and is not yet verified, a new link has been sent.");
        }
        return sendVerification(found.get());
    }

    private MessageResponse sendVerification(User user) {
        tokens.deleteByUser(user);
        VerificationToken t = new VerificationToken();
        t.setUser(user);
        t.setToken(UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", ""));
        t.setExpiresAt(Instant.now().plus(24, ChronoUnit.HOURS));
        tokens.save(t);

        String link = frontendUrl + "/verify?token=" + t.getToken();
        boolean sent = email.sendVerification(user, link);
        if (sent) {
            return MessageResponse.of("Verification email sent to " + user.getEmail()
                    + ". Open the link in it, then log in.");
        }
        if (devMode) {
            return new MessageResponse("Email sending is not configured on this server (development mode). "
                    + "Use the link below to verify your account.", link);
        }
        return MessageResponse.of("We could not send the verification email right now. Please try again later.");
    }

    @Transactional
    public MessageResponse verify(String token) {
        VerificationToken t = tokens.findByToken(token)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                        "This verification link is invalid or has already been used."));
        if (t.getExpiresAt().isBefore(Instant.now())) {
            tokens.delete(t);
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "This verification link has expired. Please request a new one.");
        }
        User user = t.getUser();
        user.setEmailVerified(true);
        users.save(user);
        tokens.delete(t);
        return MessageResponse.of("Email verified! You can now log in.");
    }

    public AuthResponse login(LoginRequest req) {
        User user = users.findByEmailIgnoreCase(req.email().trim())
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!user.isEmailVerified()) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Please verify your email before logging in. Check your inbox for the link.");
        }
        return new AuthResponse(jwt.generate(user), UserDto.of(user, true));
    }
}
