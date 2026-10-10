package com.agriculture.farm;

import com.agriculture.farm.auth.AccessService;
import com.agriculture.farm.dto.Responses.ProfitSummary;
import com.agriculture.farm.entity.Farm;
import com.agriculture.farm.exception.BadRequestException;
import com.agriculture.farm.repository.ExpenseRepository;
import com.agriculture.farm.repository.HarvestRepository;
import com.agriculture.farm.service.ProfitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfitServiceTest {
    private static final LocalDate FROM = LocalDate.of(2026, 1, 1);
    private static final LocalDate TO = LocalDate.of(2026, 12, 31);

    @Mock AccessService access;
    @Mock ExpenseRepository expenses;
    @Mock HarvestRepository harvests;
    @InjectMocks ProfitService service;

    private Farm farm(long id, String name) {
        Farm f = new Farm();
        f.setId(id);
        f.setName(name);
        return f;
    }

    private static void assertNum(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), "expected " + expected + " but was " + actual);
    }

    @Test
    void profitIsRevenueMinusExpensePerFarmAndInTotal() {
        when(access.accessibleFarms(null)).thenReturn(List.of(farm(1, "A"), farm(2, "B")));
        when(harvests.sumRevenueByFarm(eq(1L), any(), any())).thenReturn(new BigDecimal("1000"));
        when(harvests.sumRevenueByFarm(eq(2L), any(), any())).thenReturn(new BigDecimal("500"));
        when(expenses.sumAmountByFarm(eq(1L), any(), any())).thenReturn(new BigDecimal("300"));
        when(expenses.sumAmountByFarm(eq(2L), any(), any())).thenReturn(new BigDecimal("700"));

        ProfitSummary s = service.profit(FROM, TO, null);

        assertEquals(2, s.farms().size());
        assertNum("700", s.farms().get(0).profit());
        assertNum("-200", s.farms().get(1).profit());
        assertNum("1500", s.totalRevenue());
        assertNum("1000", s.totalExpense());
        assertNum("500", s.totalProfit());
    }

    @Test
    void missingValuesCountAsZero() {
        when(access.accessibleFarms(null)).thenReturn(List.of(farm(1, "A")));
        ProfitSummary s = service.profit(FROM, TO, null);
        assertNum("0", s.totalRevenue());
        assertNum("0", s.totalExpense());
        assertNum("0", s.totalProfit());
    }

    @Test
    void rejectsInvalidDateRange() {
        assertThrows(BadRequestException.class, () -> service.profit(TO, FROM, null));
        verifyNoInteractions(harvests, expenses);
    }
}
