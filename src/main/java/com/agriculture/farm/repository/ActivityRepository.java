package com.agriculture.farm.repository;

import com.agriculture.farm.entity.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.*;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    Page<Activity> findByFieldFarmFarmerId(Long farmerId, Pageable pageable);
}
