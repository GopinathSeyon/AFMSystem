package com.agriculture.farm.repository;

import com.agriculture.farm.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.*;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    Page<Expense> findByFarmFarmerId(Long farmerId, Pageable pageable);

    @Query("select sum(e.amount) from Expense e where e.farm.id = :farmId and e.expenseDate between :from and :to")
    BigDecimal sumAmountByFarm(@Param("farmId") Long farmId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select count(e) from Expense e where e.farm.id = :farmId and e.expenseDate between :from and :to")
    long countByFarm(@Param("farmId") Long farmId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
