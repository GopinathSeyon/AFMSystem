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
public class ProfitService {
    private final AccessService access;
    private final ExpenseRepository expenses;
    private final HarvestRepository harvests;

    /** Profit = total harvest revenue - total expenses (activity labour cost is not added, to avoid double counting). */
    @Transactional(readOnly = true)
    public ProfitSummary profit(LocalDate from, LocalDate to, Long farmId) {
        ServiceUtil.validateRange(from, to);
        List<FarmProfit> list = new ArrayList<>();
        BigDecimal totalRevenue = BigDecimal.ZERO, totalExpense = BigDecimal.ZERO;
        for (Farm f : access.accessibleFarms(farmId)) {
            BigDecimal revenue = ServiceUtil.nz(harvests.sumRevenueByFarm(f.getId(), from, to));
            BigDecimal expense = ServiceUtil.nz(expenses.sumAmountByFarm(f.getId(), from, to));
            list.add(new FarmProfit(f.getId(), f.getName(), revenue, expense, revenue.subtract(expense)));
            totalRevenue = totalRevenue.add(revenue);
            totalExpense = totalExpense.add(expense);
        }
        return new ProfitSummary(from, to, totalRevenue, totalExpense, totalRevenue.subtract(totalExpense), list);
    }
}
