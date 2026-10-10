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
public class CropService {
    private final CropRepository repo;
    private final FieldRepository fields;
    private final AccessService access;
    private final EntityMapper mapper;

    public CropResponse create(CropRequest r) {
        Crop c = new Crop();
        apply(c, r);
        return mapper.crop(repo.save(c));
    }

    @Transactional(readOnly = true)
    public Page<CropResponse> list(Pageable p) {
        Page<Crop> page = access.isAdmin() ? repo.findAll(p) : repo.findByFieldFarmFarmerId(access.currentFarmerId(), p);
        return page.map(mapper::crop);
    }

    @Transactional(readOnly = true)
    public Page<CropResponse> search(String name, Pageable p) {
        Page<Crop> page = access.isAdmin()
                ? repo.findByNameContainingIgnoreCase(name, p)
                : repo.findByNameContainingIgnoreCaseAndFieldFarmFarmerId(name, access.currentFarmerId(), p);
        return page.map(mapper::crop);
    }

    @Transactional(readOnly = true)
    public Page<CropResponse> byStatus(CropStatus status, Pageable p) {
        Page<Crop> page = access.isAdmin()
                ? repo.findByStatus(status, p)
                : repo.findByStatusAndFieldFarmFarmerId(status, access.currentFarmerId(), p);
        return page.map(mapper::crop);
    }

    @Transactional(readOnly = true)
    public CropResponse get(Long id) { return mapper.crop(find(id)); }

    public CropResponse update(Long id, CropRequest r) {
        Crop c = find(id);
        apply(c, r);
        return mapper.crop(repo.save(c));
    }

    public void delete(Long id) {
        access.requireAdmin();
        repo.delete(find(id));
    }

    private Crop find(Long id) {
        Crop c = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Crop not found: " + id));
        access.checkCrop(c);
        return c;
    }

    private void apply(Crop c, CropRequest r) {
        Field field = fields.findById(r.fieldId()).orElseThrow(() -> new ResourceNotFoundException("Field not found: " + r.fieldId()));
        access.checkField(field);
        if (r.plantingDate() != null && r.expectedHarvestDate() != null
                && r.expectedHarvestDate().isBefore(r.plantingDate())) {
            throw new BadRequestException("expectedHarvestDate must not be before plantingDate");
        }
        c.setName(r.name().trim());
        c.setVariety(r.variety());
        c.setPlantingDate(r.plantingDate());
        c.setExpectedHarvestDate(r.expectedHarvestDate());
        c.setPlannedQuantity(r.plannedQuantity());
        if (r.status() != null) c.setStatus(r.status());
        c.setField(field);
    }
}
