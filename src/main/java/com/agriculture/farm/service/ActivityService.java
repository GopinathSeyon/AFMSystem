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
public class ActivityService {
    private final ActivityRepository repo;
    private final FieldRepository fields;
    private final AccessService access;
    private final EntityMapper mapper;

    public ActivityResponse create(ActivityRequest r) {
        Activity a = new Activity();
        apply(a, r);
        return mapper.activity(repo.save(a));
    }

    @Transactional(readOnly = true)
    public Page<ActivityResponse> list(Pageable p) {
        Page<Activity> page = access.isAdmin() ? repo.findAll(p) : repo.findByFieldFarmFarmerId(access.currentFarmerId(), p);
        return page.map(mapper::activity);
    }

    @Transactional(readOnly = true)
    public ActivityResponse get(Long id) { return mapper.activity(find(id)); }

    public ActivityResponse update(Long id, ActivityRequest r) {
        Activity a = find(id);
        apply(a, r);
        return mapper.activity(repo.save(a));
    }

    public void delete(Long id) {
        access.requireAdmin();
        repo.delete(find(id));
    }

    private Activity find(Long id) {
        Activity a = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Activity not found: " + id));
        access.checkField(a.getField());
        return a;
    }

    private void apply(Activity a, ActivityRequest r) {
        Field field = fields.findById(r.fieldId()).orElseThrow(() -> new ResourceNotFoundException("Field not found: " + r.fieldId()));
        access.checkField(field);
        a.setActivityType(r.activityType());
        a.setDescription(r.description());
        a.setActivityDate(r.activityDate());
        a.setLabourCost(r.labourCost());
        a.setQuantity(r.quantity());
        a.setField(field);
    }
}
