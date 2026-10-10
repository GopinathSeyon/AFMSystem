package com.agriculture.farm.dto;

import com.agriculture.farm.entity.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class Responses {
    private Responses() {}

    public record AuthResponse(boolean success, String message, String tokenType, String token,
                               String refreshToken, Long userId, String name, String email,
                               Role role, Long farmerId) {}
    public record UserSummary(Long id, String name, String email, Role role, boolean active,
                              Long farmerId, LocalDateTime createdAt) {}
    public record FarmerResponse(Long id, String name, String email, String phone, String address,
                                 boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {}
    public record FarmResponse(Long id, String name, BigDecimal totalArea, FarmStatus status,
                               Long farmerId, String farmerName) {}
    public record FieldResponse(Long id, String name, BigDecimal area, String soilType, FieldStatus status,
                                Long farmId, String farmName) {}
    public record CropResponse(Long id, String name, String variety, LocalDate plantingDate,
                               LocalDate expectedHarvestDate, BigDecimal plannedQuantity, CropStatus status,
                               Long fieldId, String fieldName) {}
    public record ActivityResponse(Long id, ActivityType activityType, String description, LocalDate activityDate,
                                   BigDecimal labourCost, BigDecimal quantity, Long fieldId, String fieldName) {}
    public record ExpenseResponse(Long id, ExpenseCategory category, BigDecimal amount, LocalDate expenseDate,
                                  String description, Long farmId, String farmName) {}
    public record HarvestResponse(Long id, LocalDate harvestDate, BigDecimal quantity, String unit,
                                  BigDecimal pricePerUnit, BigDecimal totalRevenue, Long cropId, String cropName) {}
    public record ExpenseReport(LocalDate from, LocalDate to, Long farmId, BigDecimal totalAmount, long count) {}
    public record HarvestReport(LocalDate from, LocalDate to, BigDecimal totalQuantity,
                                BigDecimal totalRevenue, long count) {}
    public record FarmProfit(Long farmId, String farmName, BigDecimal revenue, BigDecimal expense, BigDecimal profit) {}
    public record ProfitSummary(LocalDate from, LocalDate to, BigDecimal totalRevenue, BigDecimal totalExpense,
                                BigDecimal totalProfit, List<FarmProfit> farms) {}
}
