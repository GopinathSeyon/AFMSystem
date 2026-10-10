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

public final class ServiceUtil {
    private ServiceUtil() {}

    public static BigDecimal nz(BigDecimal v) { return v == null ? BigDecimal.ZERO : v; }

    public static void validateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null) throw new BadRequestException("Both 'from' and 'to' dates are required");
        if (from.isAfter(to)) throw new BadRequestException("'from' date must not be after 'to' date");
    }
}
