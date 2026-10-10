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
public class FieldService {
    private final FieldRepository repo;
    private final FarmRepository farms;
    private final AccessService access;
    private final EntityMapper mapper;

    public FieldResponse create(FieldRequest r) {
        Field f = new Field();
        apply(f, r);
        return mapper.field(repo.save(f));
    }

    @Transactional(readOnly = true)
    public Page<FieldResponse> list(Pageable p) {
        Page<Field> page = access.isAdmin() ? repo.findAll(p) : repo.findByFarmFarmerId(access.currentFarmerId(), p);
        return page.map(mapper::field);
    }

    @Transactional(readOnly = true)
    public FieldResponse get(Long id) { return mapper.field(find(id)); }

    public FieldResponse update(Long id, FieldRequest r) {
        Field f = find(id);
        apply(f, r);
        return mapper.field(repo.save(f));
    }

    public void delete(Long id) {
        access.requireAdmin();
        repo.delete(find(id));
    }

    private Field find(Long id) {
        Field f = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Field not found: " + id));
        access.checkField(f);
        return f;
    }

    private void apply(Field f, FieldRequest r) {
        Farm farm = farms.findById(r.farmId()).orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + r.farmId()));
        access.checkFarm(farm);
        f.setName(r.name().trim());
        f.setArea(r.area());
        f.setSoilType(r.soilType());
        if (r.status() != null) f.setStatus(r.status());
        f.setFarm(farm);
    }
}
