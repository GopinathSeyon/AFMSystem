package com.agriculture.farm.auth;

import com.agriculture.farm.dto.Requests.*;
import com.agriculture.farm.dto.Responses.AuthResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest r) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.register(r));
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) { return service.login(r); }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest r) { return service.refresh(r); }

    @PostMapping("/logout")
    public Map<String, Object> logout(@Valid @RequestBody RefreshRequest r) {
        service.logout(r);
        return Map.of("success", true, "message", "Logged out");
    }
}
