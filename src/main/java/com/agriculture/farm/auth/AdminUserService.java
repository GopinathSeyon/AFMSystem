package com.agriculture.farm.auth;

import com.agriculture.farm.dto.Responses.UserSummary;
import com.agriculture.farm.entity.AppUser;
import com.agriculture.farm.entity.Farmer;
import com.agriculture.farm.entity.Role;
import com.agriculture.farm.exception.BadRequestException;
import com.agriculture.farm.exception.ResourceNotFoundException;
import com.agriculture.farm.mapper.EntityMapper;
import com.agriculture.farm.repository.AppUserRepository;
import com.agriculture.farm.repository.FarmerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminUserService {
    private final AppUserRepository users;
    private final FarmerRepository farmers;
    private final AccessService access;
    private final FarmerProfileLinker linker;
    private final RefreshTokenService refreshTokens;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public Page<UserSummary> list(Pageable p) { return users.findAll(p).map(mapper::user); }

    @Transactional(readOnly = true)
    public UserSummary get(Long id) { return mapper.user(find(id)); }

    public UserSummary changeRole(Long id, Role role) {
        AppUser u = find(id);
        if (isSelf(u) && role != Role.ADMIN) throw new BadRequestException("You cannot remove your own ADMIN role");
        u.setRole(role);
        if (role == Role.FARMER) linker.ensureProfile(u);
        return mapper.user(users.save(u));
    }

    public UserSummary setActive(Long id, boolean active) {
        AppUser u = find(id);
        if (isSelf(u) && !active) throw new BadRequestException("You cannot deactivate your own account");
        u.setActive(active);
        if (!active) refreshTokens.revokeAll(u.getId());
        return mapper.user(users.save(u));
    }

    public UserSummary linkFarmer(Long id, Long farmerId) {
        AppUser u = find(id);
        Farmer f = farmers.findById(farmerId)
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found: " + farmerId));
        users.findByFarmerId(farmerId).filter(o -> !o.getId().equals(u.getId())).ifPresent(o -> {
            throw new BadRequestException("This farmer profile is already linked to another account");
        });
        u.setFarmer(f);
        return mapper.user(users.save(u));
    }

    private boolean isSelf(AppUser u) { return u.getId().equals(access.currentUser().getId()); }

    private AppUser find(Long id) {
        return users.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
