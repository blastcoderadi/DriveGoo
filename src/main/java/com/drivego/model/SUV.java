package com.drivego.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * OOP Concept: INHERITANCE + POLYMORPHISM
 *
 * SUV extends Vehicle.
 * SUVs add a 10% premium surcharge on top of the base rental cost.
 */
@Entity
@DiscriminatorValue("SUV")
public class SUV extends Vehicle {

    private static final BigDecimal PREMIUM_SURCHARGE = new BigDecimal("0.10"); // 10%

    @Column(name = "four_wheel_drive")
    private boolean fourWheelDrive;

    public SUV() { super(); }

    public SUV(String brand, String model, String registrationNumber,
               BigDecimal pricePerDay, BigDecimal securityDeposit, boolean fourWheelDrive) {
        super(brand, model, registrationNumber, pricePerDay, securityDeposit);
        this.fourWheelDrive = fourWheelDrive;
    }

    @Override
    public String getVehicleCategory() { return "SUV"; }

    /**
     * OOP Concept: METHOD OVERRIDING
     * SUVs charge 10% of (pricePerDay × days) as a premium surcharge.
     */
    @Override
    public BigDecimal calculateCategoryCharge(int rentalDays) {
        return getPricePerDay()
                .multiply(new BigDecimal(rentalDays))
                .multiply(PREMIUM_SURCHARGE)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public boolean isFourWheelDrive() { return fourWheelDrive; }
    public void setFourWheelDrive(boolean fourWheelDrive) { this.fourWheelDrive = fourWheelDrive; }
}
