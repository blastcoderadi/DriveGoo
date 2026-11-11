package com.drivego.service;

import com.drivego.dto.PriceBreakdownDTO;
import com.drivego.model.Vehicle;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * OOP Concept: INTERFACE + METHOD OVERLOADING (defined in impl)
 *
 * PricingService defines pricing calculation contracts.
 * Three overloads of calculatePrice() demonstrate METHOD OVERLOADING.
 */
public interface PricingService {

    /** Calculate price breakdown from pickup and return dates */
    PriceBreakdownDTO calculatePrice(Vehicle vehicle, LocalDate pickupDate, LocalDate returnDate);

    /** Calculate price breakdown from number of days */
    PriceBreakdownDTO calculatePrice(Vehicle vehicle, int days);

    /** Calculate price with discount applied */
    PriceBreakdownDTO calculatePrice(Vehicle vehicle, int days, BigDecimal discountAmount);
}
