package com.agriculture.farm.auth;

import com.agriculture.farm.entity.AppUser;
import com.agriculture.farm.entity.Role;
import com.agriculture.farm.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the first ADMIN from ADMIN_EMAIL / ADMIN_PASSWORD (once). */
@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer implements ApplicationRunner {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email:}") private String email;
    @Value("${app.admin.password:}") private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            log.info("ADMIN_EMAIL / ADMIN_PASSWORD not set: no admin account created.");
            return;
        }
        if (password.length() < 8) throw new IllegalStateException("ADMIN_PASSWORD must be at least 8 characters");
        String e = email.trim().toLowerCase();
        if (users.existsByEmail(e)) return;
        AppUser admin = new AppUser();
        admin.setName("Administrator");
        admin.setEmail(e);
        admin.setPassword(encoder.encode(password));
        admin.setRole(Role.ADMIN);
        users.save(admin);
        log.info("First ADMIN account created: {}", e);
    }
}
