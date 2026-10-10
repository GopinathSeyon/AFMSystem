package com.agriculture.farm.mapper;

import com.agriculture.farm.dto.Responses.*;
import com.agriculture.farm.entity.*;
import org.springframework.stereotype.Component;

@Component
public class EntityMapper {

    public UserSummary user(AppUser u) {
        return new UserSummary(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.isActive(),
                u.getFarmer() == null ? null : u.getFarmer().getId(), u.getCreatedAt());
    }
    public FarmerResponse farmer(Farmer f) {
        return new FarmerResponse(f.getId(), f.getName(), f.getEmail(), f.getPhone(), f.getAddress(),
                f.isActive(), f.getCreatedAt(), f.getUpdatedAt());
    }
    public FarmResponse farm(Farm f) {
        return new FarmResponse(f.getId(), f.getName(), f.getTotalArea(), f.getStatus(),
                f.getFarmer().getId(), f.getFarmer().getName());
    }
    public FieldResponse field(Field f) {
        return new FieldResponse(f.getId(), f.getName(), f.getArea(), f.getSoilType(), f.getStatus(),
                f.getFarm().getId(), f.getFarm().getName());
    }
    public CropResponse crop(Crop c) {
        return new CropResponse(c.getId(), c.getName(), c.getVariety(), c.getPlantingDate(), c.getExpectedHarvestDate(),
                c.getPlannedQuantity(), c.getStatus(), c.getField().getId(), c.getField().getName());
    }
    public ActivityResponse activity(Activity a) {
        return new ActivityResponse(a.getId(), a.getActivityType(), a.getDescription(), a.getActivityDate(),
                a.getLabourCost(), a.getQuantity(), a.getField().getId(), a.getField().getName());
    }
    public ExpenseResponse expense(Expense e) {
        return new ExpenseResponse(e.getId(), e.getCategory(), e.getAmount(), e.getExpenseDate(), e.getDescription(),
                e.getFarm().getId(), e.getFarm().getName());
    }
    public HarvestResponse harvest(Harvest h) {
        return new HarvestResponse(h.getId(), h.getHarvestDate(), h.getQuantity(), h.getUnit(), h.getPricePerUnit(),
                h.getTotalRevenue(), h.getCrop().getId(), h.getCrop().getName());
    }
}
