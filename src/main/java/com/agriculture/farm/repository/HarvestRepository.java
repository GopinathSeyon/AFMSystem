package com.agriculture.farm.repository;

import com.agriculture.farm.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.*;

public interface HarvestRepository extends JpaRepository<Harvest, Long> {
    Page<Harvest> findByCropFieldFarmFarmerId(Long farmerId, Pageable pageable);

    @Query("select sum(h.totalRevenue) from Harvest h where h.crop.field.farm.id = :farmId and h.harvestDate between :from and :to")
    BigDecimal sumRevenueByFarm(@Param("farmId") Long farmId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select sum(h.quantity) from Harvest h where h.crop.field.farm.id = :farmId and h.harvestDate between :from and :to")
    BigDecimal sumQuantityByFarm(@Param("farmId") Long farmId, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select count(h) from Harvest h where h.crop.field.farm.id = :farmId and h.harvestDate between :from and :to")
    long countByFarm(@Param("farmId") Long farmId, @Param("from") LocalDate from, @Param("to") LocalDate to);
}
