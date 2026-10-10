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
public class HarvestService {
    private final HarvestRepository repo;
    private final CropRepository crops;
    private final AccessService access;
    private final EntityMapper mapper;

    public HarvestResponse create(HarvestRequest r) {
        Harvest h = new Harvest();
        Crop crop = apply(h, r);
        crop.setStatus(CropStatus.HARVESTED);   // harvesting a crop marks it HARVESTED
        return mapper.harvest(repo.save(h));
    }

    @Transactional(readOnly = true)
    public Page<HarvestResponse> list(Pageable p) {
        Page<Harvest> page = access.isAdmin() ? repo.findAll(p) : repo.findByCropFieldFarmFarmerId(access.currentFarmerId(), p);
        return page.map(mapper::harvest);
    }

    @Transactional(readOnly = true)
    public HarvestResponse get(Long id) { return mapper.harvest(find(id)); }

    public HarvestResponse update(Long id, HarvestRequest r) {
        Harvest h = find(id);
        apply(h, r);
        return mapper.harvest(repo.save(h));
    }

    public void delete(Long id) {
        access.requireAdmin();
        repo.delete(find(id));
    }

    @Transactional(readOnly = true)
    public HarvestReport report(LocalDate from, LocalDate to) {
        ServiceUtil.validateRange(from, to);
        BigDecimal qty = BigDecimal.ZERO, revenue = BigDecimal.ZERO;
        long count = 0;
        for (Farm f : access.accessibleFarms(null)) {
            qty = qty.add(ServiceUtil.nz(repo.sumQuantityByFarm(f.getId(), from, to)));
            revenue = revenue.add(ServiceUtil.nz(repo.sumRevenueByFarm(f.getId(), from, to)));
            count += repo.countByFarm(f.getId(), from, to);
        }
        return new HarvestReport(from, to, qty, revenue, count);
    }

    private Harvest find(Long id) {
        Harvest h = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Harvest not found: " + id));
        access.checkCrop(h.getCrop());
        return h;
    }

    private Crop apply(Harvest h, HarvestRequest r) {
        Crop crop = crops.findById(r.cropId()).orElseThrow(() -> new ResourceNotFoundException("Crop not found: " + r.cropId()));
        access.checkCrop(crop);
        h.setHarvestDate(r.harvestDate());
        h.setQuantity(r.quantity());
        h.setUnit(r.unit());
        h.setPricePerUnit(r.pricePerUnit());
        // Total Revenue = Quantity x Price per Unit
        h.setTotalRevenue(r.quantity().multiply(r.pricePerUnit()).setScale(2, RoundingMode.HALF_UP));
        h.setCrop(crop);
        return crop;
    }
}
