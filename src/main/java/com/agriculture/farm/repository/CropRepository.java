package com.agriculture.farm.repository;

import com.agriculture.farm.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.*;

public interface CropRepository extends JpaRepository<Crop, Long> {
    Page<Crop> findByFieldFarmFarmerId(Long farmerId, Pageable pageable);
    Page<Crop> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Crop> findByNameContainingIgnoreCaseAndFieldFarmFarmerId(String name, Long farmerId, Pageable pageable);
    Page<Crop> findByStatus(CropStatus status, Pageable pageable);
    Page<Crop> findByStatusAndFieldFarmFarmerId(CropStatus status, Long farmerId, Pageable pageable);
}
