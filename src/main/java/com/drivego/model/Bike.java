package com.drivego.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * OOP Concept: INHERITANCE + POLYMORPHISM
 *
 * Bike extends Vehicle.
 * Bikes add a ₹200/day helmet & insurance surcharge.
 * This shows how calculateCategoryCharge() behaves differently per subclass.
 */
@Entity
@DiscriminatorValue("BIKE")
public class Bike extends Vehicle {

    private static final BigDecimal HELMET_INSURANCE_PER_DAY = new BigDecimal("200");

    @Column(name = "bike_type")
    private String bikeType; // Sport, Cruiser, Scooter

    public Bike() { super(); }

    public Bike(String brand, String model, String registrationNumber,
                BigDecimal pricePerDay, BigDecimal securityDeposit, String bikeType) {
        super(brand, model, registrationNumber, pricePerDay, securityDeposit);
        this.bikeType = bikeType;
    }

    @Override
    public String getVehicleCategory() { return "Bike"; }

    /**
     * OOP Concept: METHOD OVERRIDING
     * Bikes charge ₹200/day for mandatory helmet and insurance.
     */
    @Override
    public BigDecimal calculateCategoryCharge(int rentalDays) {
        return HELMET_INSURANCE_PER_DAY.multiply(new BigDecimal(rentalDays));
    }

    public String getBikeType() { return bikeType; }
    public void setBikeType(String bikeType) { this.bikeType = bikeType; }
}
