package com.agriculture.farm.auth;

import com.agriculture.farm.dto.Requests.*;
import com.agriculture.farm.dto.Responses.AuthResponse;
import com.agriculture.farm.entity.AppUser;
import com.agriculture.farm.entity.Role;
import com.agriculture.farm.exception.ConflictException;
import com.agriculture.farm.repository.AppUserRepository;
import com.agriculture.farm.repository.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AppUserRepository users;
    private final FarmerRepository farmers;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final RefreshTokenService refreshTokens;
    private final FarmerProfileLinker linker;

    @Transactional
    public AuthResponse register(RegisterRequest r) {
        String email = r.email().trim().toLowerCase();
        if (users.existsByEmail(email)) throw new ConflictException("Email is already registered");
        if (farmers.existsByEmail(email)) {
            throw new ConflictException("A farmer profile with this email already exists; ask an admin to link it");
        }
        AppUser u = new AppUser();
        u.setName(r.name().trim());
        u.setEmail(email);
        u.setPassword(encoder.encode(r.password()));
        u.setRole(Role.FARMER);          // role can never be chosen by the caller
        u.setActive(true);
        users.save(u);
        linker.ensureProfile(u);
        return build(u, "Registration successful");
    }

    @Transactional
    public AuthResponse login(LoginRequest r) {
        String email = r.email().trim().toLowerCase();
        authManager.authenticate(new UsernamePasswordAuthenticationToken(email, r.password()));
        AppUser u = users.findByEmail(email).orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        linker.ensureProfile(u);
        return build(u, "Login successful");
    }

    // Deliberately not transactional as a whole: reuse detection must commit even though it throws.
    public AuthResponse refresh(RefreshRequest r) {
        AppUser u = refreshTokens.rotate(r.refreshToken());
        return build(u, "Token refreshed");
    }

    public void logout(RefreshRequest r) { refreshTokens.revoke(r.refreshToken()); }

    private AuthResponse build(AppUser u, String message) {
        String access = jwt.generateToken(u);
        String refresh = refreshTokens.issue(u);
        Long farmerId = u.getFarmer() == null ? null : u.getFarmer().getId();
        return new AuthResponse(true, message, "Bearer", access, refresh, u.getId(), u.getName(),
                u.getEmail(), u.getRole(), farmerId);
    }
}
