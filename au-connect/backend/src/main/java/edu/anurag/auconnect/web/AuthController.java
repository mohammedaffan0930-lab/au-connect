package edu.anurag.auconnect.web;

import edu.anurag.auconnect.dto.Dtos.*;
import edu.anurag.auconnect.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/register")
    public MessageResponse register(@Valid @RequestBody RegisterRequest req) {
        return auth.register(req);
    }

    @GetMapping("/verify")
    public MessageResponse verify(@RequestParam String token) {
        return auth.verify(token);
    }

    @PostMapping("/resend")
    public MessageResponse resend(@Valid @RequestBody ResendRequest req) {
        return auth.resend(req.email());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return auth.login(req);
    }
}
