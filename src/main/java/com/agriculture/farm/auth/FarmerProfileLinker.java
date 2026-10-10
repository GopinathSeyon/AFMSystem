package com.agriculture.farm.auth;

import com.agriculture.farm.entity.AppUser;
import com.agriculture.farm.entity.Farmer;
import com.agriculture.farm.entity.Role;
import com.agriculture.farm.repository.AppUserRepository;
import com.agriculture.farm.repository.FarmerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class FarmerProfileLinker {
    private final FarmerRepository farmers;
    private final AppUserRepository users;

    /** Creates and links a Farmer profile for a FARMER account that has none. Call inside a transaction. */
    public void ensureProfile(AppUser u) {
        if (u.getRole() != Role.FARMER || u.getFarmer() != null) return;
        if (farmers.existsByEmail(u.getEmail())) {
            log.warn("A farmer profile with email {} already exists; an admin must link it.", u.getEmail());
            return;
        }
        Farmer f = new Farmer();
        f.setName(u.getName());
        f.setEmail(u.getEmail());
        farmers.save(f);
        u.setFarmer(f);
        users.save(u);
    }
}
