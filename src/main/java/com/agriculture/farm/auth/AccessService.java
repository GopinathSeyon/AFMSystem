package com.agriculture.farm.auth;

import com.agriculture.farm.entity.*;
import com.agriculture.farm.exception.ResourceNotFoundException;
import com.agriculture.farm.repository.FarmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.util.List;

/** Ownership checks: a FARMER may only touch records under their own farms; ADMIN is unrestricted. */
@Service
@RequiredArgsConstructor
public class AccessService {
    private final FarmRepository farms;

    public AppUser currentUser() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !(a.getPrincipal() instanceof AppUser u)) {
            throw new AccessDeniedException("Not authenticated");
        }
        return u;
    }

    public boolean isAdmin() { return currentUser().getRole() == Role.ADMIN; }

    public void requireAdmin() {
        if (!isAdmin()) throw new AccessDeniedException("Only an ADMIN can do this");
    }

    public Long currentFarmerId() {
        Farmer f = currentUser().getFarmer();
        return f == null ? null : f.getId();
    }

    public void checkFarmer(Long farmerId) {
        if (isAdmin()) return;
        Long mine = currentFarmerId();
        if (mine == null || !mine.equals(farmerId)) {
            throw new AccessDeniedException("You can only access your own data");
        }
    }

    public void checkFarm(Farm farm) { checkFarmer(farm.getFarmer().getId()); }
    public void checkField(Field field) { checkFarm(field.getFarm()); }
    public void checkCrop(Crop crop) { checkField(crop.getField()); }

    /** Farms the caller may report on: one checked farm, or all own farms (all farms for ADMIN). */
    public List<Farm> accessibleFarms(Long farmId) {
        if (farmId != null) {
            Farm f = farms.findById(farmId).orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + farmId));
            checkFarm(f);
            return List.of(f);
        }
        if (isAdmin()) return farms.findAll();
        Long mine = currentFarmerId();
        return mine == null ? List.of() : farms.findByFarmerId(mine);
    }
}
