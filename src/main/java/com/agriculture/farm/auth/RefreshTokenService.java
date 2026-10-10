package com.agriculture.farm.auth;

import com.agriculture.farm.entity.AppUser;
import com.agriculture.farm.entity.RefreshToken;
import com.agriculture.farm.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private static final SecureRandom RNG = new SecureRandom();
    private final RefreshTokenRepository repo;

    @Value("${app.jwt.refresh-expiration-ms}")
    private long refreshMs;

    @Transactional
    public String issue(AppUser user) {
        byte[] bytes = new byte[48];
        RNG.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        RefreshToken t = new RefreshToken();
        t.setUser(user);
        t.setTokenHash(hash(raw));
        t.setExpiresAt(Instant.now().plusMillis(refreshMs));
        repo.save(t);
        return raw;
    }

    /** Each refresh token works once. Re-using a used token revokes every token of that user. */
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public AppUser rotate(String raw) {
        RefreshToken t = repo.findByTokenHash(hash(raw))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));
        if (t.isRevoked()) {
            repo.revokeAllByUser(t.getUser().getId());
            throw new BadCredentialsException("Refresh token already used");
        }
        if (t.getExpiresAt().isBefore(Instant.now())) throw new BadCredentialsException("Refresh token expired");
        if (!t.getUser().isActive()) throw new BadCredentialsException("Account is deactivated");
        t.setRevoked(true);
        return t.getUser();
    }

    @Transactional
    public void revoke(String raw) {
        repo.findByTokenHash(hash(raw)).ifPresent(t -> t.setRevoked(true));
    }

    @Transactional
    public void revokeAll(Long userId) { repo.revokeAllByUser(userId); }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void purgeExpired() { repo.deleteExpired(Instant.now()); }

    private static String hash(String raw) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
