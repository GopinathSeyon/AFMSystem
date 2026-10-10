package com.agriculture.farm.dto;

import com.agriculture.farm.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class Requests {
    private Requests() {}

    public record RegisterRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @NotBlank @Size(min = 8, max = 72) String password) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}
    public record RefreshRequest(@NotBlank String refreshToken) {}
    public record RoleRequest(@NotNull Role role) {}
    public record ActiveRequest(@NotNull Boolean active) {}
    public record LinkFarmerRequest(@NotNull Long farmerId) {}

    public record FarmerRequest(
            @NotBlank @Size(max = 100) String name,
            @NotBlank @Email @Size(max = 150) String email,
            @Size(max = 20) String phone,
            @Size(max = 255) String address,
            Boolean active) {}
    public record FarmRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @Positive BigDecimal totalArea,
            FarmStatus status,
            @NotNull Long farmerId) {}
    public record FieldRequest(
            @NotBlank @Size(max = 100) String name,
            @NotNull @Positive BigDecimal area,
            @Size(max = 50) String soilType,
            FieldStatus status,
            @NotNull Long farmId) {}
    public record CropRequest(
            @NotBlank @Size(max = 100) String name,
            @Size(max = 100) String variety,
            LocalDate plantingDate,
            LocalDate expectedHarvestDate,
            @PositiveOrZero BigDecimal plannedQuantity,
            CropStatus status,
            @NotNull Long fieldId) {}
    public record ActivityRequest(
            @NotNull ActivityType activityType,
            @Size(max = 255) String description,
            @NotNull LocalDate activityDate,
            @PositiveOrZero BigDecimal labourCost,
            @PositiveOrZero BigDecimal quantity,
            @NotNull Long fieldId) {}
    public record ExpenseRequest(
            @NotNull ExpenseCategory category,
            @NotNull @PositiveOrZero BigDecimal amount,
            @NotNull LocalDate expenseDate,
            @Size(max = 255) String description,
            @NotNull Long farmId) {}
    public record HarvestRequest(
            @NotNull LocalDate harvestDate,
            @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal quantity,
            @Size(max = 20) String unit,
            @NotNull @PositiveOrZero BigDecimal pricePerUnit,
            @NotNull Long cropId) {}
}
