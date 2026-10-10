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
public class ExpenseService {
    private final ExpenseRepository repo;
    private final FarmRepository farms;
    private final AccessService access;
    private final EntityMapper mapper;

    public ExpenseResponse create(ExpenseRequest r) {
        Expense e = new Expense();
        apply(e, r);
        return mapper.expense(repo.save(e));
    }

    @Transactional(readOnly = true)
    public Page<ExpenseResponse> list(Pageable p) {
        Page<Expense> page = access.isAdmin() ? repo.findAll(p) : repo.findByFarmFarmerId(access.currentFarmerId(), p);
        return page.map(mapper::expense);
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) { return mapper.expense(find(id)); }

    public ExpenseResponse update(Long id, ExpenseRequest r) {
        Expense e = find(id);
        apply(e, r);
        return mapper.expense(repo.save(e));
    }

    public void delete(Long id) {
        access.requireAdmin();
        repo.delete(find(id));
    }

    @Transactional(readOnly = true)
    public ExpenseReport report(LocalDate from, LocalDate to, Long farmId) {
        ServiceUtil.validateRange(from, to);
        BigDecimal total = BigDecimal.ZERO;
        long count = 0;
        for (Farm f : access.accessibleFarms(farmId)) {
            total = total.add(ServiceUtil.nz(repo.sumAmountByFarm(f.getId(), from, to)));
            count += repo.countByFarm(f.getId(), from, to);
        }
        return new ExpenseReport(from, to, farmId, total, count);
    }

    private Expense find(Long id) {
        Expense e = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        access.checkFarm(e.getFarm());
        return e;
    }

    private void apply(Expense e, ExpenseRequest r) {
        Farm farm = farms.findById(r.farmId()).orElseThrow(() -> new ResourceNotFoundException("Farm not found: " + r.farmId()));
        access.checkFarm(farm);
        e.setCategory(r.category());
        e.setAmount(r.amount());
        e.setExpenseDate(r.expenseDate());
        e.setDescription(r.description());
        e.setFarm(farm);
    }
}
