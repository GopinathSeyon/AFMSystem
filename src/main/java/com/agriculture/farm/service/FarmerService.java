package com.agriculture.farm.service;

import com.agriculture.farm.auth.AccessService;
import com.agriculture.farm.dto.Requests.*;
import com.agriculture.farm.dto.Responses.*;
import com.agriculture.farm.entity.*;
import com.agriculture.farm.exception.*;
import com.agriculture.farm.mapper.EntityMapper;
import com.agriculture.farm.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class FarmerService {
    private final FarmerRepository repo;
    private final AppUserRepository users;
    private final AccessService access;
    private final EntityMapper mapper;

    public FarmerResponse create(FarmerRequest r) {
        if (!access.isAdmin()) throw new AccessDeniedException("Farmers cannot create extra farmer profiles");
        String email = r.email().trim().toLowerCase();
        if (repo.existsByEmail(email)) throw new BadRequestException("Farmer email already exists");
        Farmer f = new Farmer();
        apply(f, r, email);
        return mapper.farmer(repo.save(f));
    }

    @Transactional(readOnly = true)
    public Page<FarmerResponse> list(Pageable p) {
        access.requireAdmin();
        return repo.findAll(p).map(mapper::farmer);
    }

    @Transactional(readOnly = true)
    public FarmerResponse me() {
        Long id = access.currentFarmerId();
        if (id == null) throw new ResourceNotFoundException("No farmer profile is linked to this account");
        return mapper.farmer(find(id));
    }

    @Transactional(readOnly = true)
    public FarmerResponse get(Long id) { return mapper.farmer(find(id)); }

    public FarmerResponse update(Long id, FarmerRequest r) {
        Farmer f = find(id);
        String email = r.email().trim().toLowerCase();
        if (!email.equals(f.getEmail()) && repo.existsByEmail(email)) throw new BadRequestException("Farmer email already exists");
        apply(f, r, email);
        return mapper.farmer(repo.save(f));
    }

    public void delete(Long id) {
        access.requireAdmin();
        Farmer f = find(id);
        users.findByFarmerId(id).ifPresent(u -> { u.setFarmer(null); users.saveAndFlush(u); });
        repo.delete(f);
    }

    private Farmer find(Long id) {
        Farmer f = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Farmer not found: " + id));
        access.checkFarmer(f.getId());
        return f;
    }

    private void apply(Farmer f, FarmerRequest r, String email) {
        f.setName(r.name().trim());
        f.setEmail(email);
        f.setPhone(r.phone());
        f.setAddress(r.address());
        if (r.active() != null) f.setActive(r.active());
    }
}
