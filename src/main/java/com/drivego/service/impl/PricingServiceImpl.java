package com.drivego.service.impl;

import com.drivego.dto.PriceBreakdownDTO;
import com.drivego.model.Vehicle;
import com.drivego.service.PricingService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * OOP Concept: INTERFACE IMPLEMENTATION + METHOD OVERLOADING
 *
 * PricingServiceImpl implements PricingService.
 * Three overloads of calculatePrice() show METHOD OVERLOADING (compile-time polymorphism).
 *
 * Price formula:
 *   baseRent        = pricePerDay × days
 *   categoryCharge  = vehicle.calculateCategoryCharge(days)  [polymorphism]
 *   taxAmount       = 5% × (baseRent + categoryCharge)
 *   total           = baseRent + categoryCharge + taxAmount + securityDeposit
 */
@Service
public class PricingServiceImpl implements PricingService {

    private static final BigDecimal TAX_RATE = Vehicle.TAX_RATE; // 5%

    /**
     * OOP Concept: METHOD OVERLOADING – Version 1 (from dates)
     */
    @Override
    public PriceBreakdownDTO calculatePrice(Vehicle vehicle, LocalDate pickupDate, LocalDate returnDate) {
        int days = (int) Math.max(1, ChronoUnit.DAYS.between(pickupDate, returnDate));
        return calculatePrice(vehicle, days);
    }

    /**
     * OOP Concept: METHOD OVERLOADING – Version 2 (from days)
     */
    @Override
    public PriceBreakdownDTO calculatePrice(Vehicle vehicle, int days) {
        return calculatePrice(vehicle, days, BigDecimal.ZERO);
    }

    /**
     * OOP Concept: METHOD OVERLOADING – Version 3 (days + discount)
     * OOP Concept: POLYMORPHISM – vehicle.calculateCategoryCharge() calls
     *   the correct subclass version (Car/Bike/SUV) at runtime.
     */
    @Override
    public PriceBreakdownDTO calculatePrice(Vehicle vehicle, int days, BigDecimal discountAmount) {
        if (discountAmount == null) discountAmount = BigDecimal.ZERO;

        BigDecimal dailyRate = vehicle.getPricePerDay();
        BigDecimal baseRent = dailyRate.multiply(new BigDecimal(days)).setScale(2, RoundingMode.HALF_UP);

        // OOP Concept: POLYMORPHISM – calls Car.calculateCategoryCharge() or Bike's or SUV's
        BigDecimal categoryCharge = vehicle.calculateCategoryCharge(days).setScale(2, RoundingMode.HALF_UP);

        BigDecimal taxable = baseRent.add(categoryCharge).subtract(discountAmount);
        BigDecimal taxAmount = taxable.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal securityDeposit = vehicle.getSecurityDeposit();
        BigDecimal total = baseRent.add(categoryCharge).add(taxAmount)
                                   .subtract(discountAmount).add(securityDeposit)
                                   .setScale(2, RoundingMode.HALF_UP);

        PriceBreakdownDTO dto = new PriceBreakdownDTO();
        dto.setRentalDays(days);
        dto.setDailyRate(dailyRate);
        dto.setBaseRent(baseRent);
        dto.setCategoryCharge(categoryCharge);
        dto.setTaxAmount(taxAmount);
        dto.setSecurityDeposit(securityDeposit);
        dto.setTotalAmount(total);
        dto.setVehicleName(vehicle.getDisplayName());
        dto.setVehicleCategory(vehicle.getVehicleCategory());
        return dto;
    }
}
