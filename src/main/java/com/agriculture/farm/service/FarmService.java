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
public class FarmService {
    private final FarmRepository repo;
    private final FarmerRepository farmers;
    private final AccessService access;
    private final EntityMapper mapper;

    public FarmResponse create(FarmRequest r) {
        Farm f = new Farm();
        apply(f, r);
        return mapper.farm(repo.save(f));
    }

    @Transactional(readOnly = true)
    public Page<FarmResponse> list(Pageable p) {
        Page<Farm> page = access.isAdmin() ? repo.findAll(p) : repo.findByFarmerId(access.currentFarmerId(), p);
        return page.map(mapper::farm);
    }

    @Transactional(readOnly = true)
    public FarmResponse get(Long id) { return mapper.farm(find(id)); }

    public FarmResponse update(Long id, FarmRequest r) {
        Farm f = find(id);
        apply(f, r);
        return mapper.farm(repo.save(f));
    }

    public void delete(Long id) {
        access.requireAdmin();
        repo.delete(find(id));
    }

    private Farm find(Long id) {
        Farm f = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + id));
        access.checkFarm(f);
        return f;
    }

    private void apply(Farm f, FarmRequest r) {
        Farmer owner = farmers.findById(r.farmerId())
                .orElseThrow(() -> new ResourceNotFoundException("Farmer not found: " + r.farmerId()));
        access.checkFarmer(owner.getId());     // cannot create or move a farm under another farmer
        f.setName(r.name().trim());
        f.setTotalArea(r.totalArea());
        if (r.status() != null) f.setStatus(r.status());
        f.setFarmer(owner);
    }
}
