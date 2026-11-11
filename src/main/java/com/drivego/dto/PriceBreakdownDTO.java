package com.drivego.dto;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OOP Concept: ENCAPSULATION
 * PriceBreakdownDTO holds the full price calculation result.
 * Uses LinkedHashMap to maintain insertion order for display.
 */
public class PriceBreakdownDTO {

    private int rentalDays;
    private BigDecimal dailyRate;
    private BigDecimal baseRent;        // dailyRate × days
    private BigDecimal categoryCharge;  // Vehicle-type surcharge
    private BigDecimal taxAmount;       // 5% of (baseRent + categoryCharge)
    private BigDecimal securityDeposit; // Fixed deposit
    private BigDecimal totalAmount;     // All above added up
    private String vehicleName;
    private String vehicleCategory;

    // LinkedHashMap preserves insertion order – shown as a table on UI
    public Map<String, BigDecimal> getBreakdownMap() {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        map.put("Base Rent (" + rentalDays + " days × ₹" + dailyRate + ")", baseRent);
        map.put("Category Charge", categoryCharge);
        map.put("GST (5%)", taxAmount);
        map.put("Security Deposit (Refundable)", securityDeposit);
        return map;
    }

    // ===== Getters and Setters =====
    public int getRentalDays() { return rentalDays; }
    public void setRentalDays(int rentalDays) { this.rentalDays = rentalDays; }

    public BigDecimal getDailyRate() { return dailyRate; }
    public void setDailyRate(BigDecimal dailyRate) { this.dailyRate = dailyRate; }

    public BigDecimal getBaseRent() { return baseRent; }
    public void setBaseRent(BigDecimal baseRent) { this.baseRent = baseRent; }

    public BigDecimal getCategoryCharge() { return categoryCharge; }
    public void setCategoryCharge(BigDecimal categoryCharge) { this.categoryCharge = categoryCharge; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(BigDecimal securityDeposit) { this.securityDeposit = securityDeposit; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getVehicleName() { return vehicleName; }
    public void setVehicleName(String vehicleName) { this.vehicleName = vehicleName; }

    public String getVehicleCategory() { return vehicleCategory; }
    public void setVehicleCategory(String vehicleCategory) { this.vehicleCategory = vehicleCategory; }
}

