package com.drivego.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * OOP Concept: INHERITANCE + ABSTRACTION + POLYMORPHISM
 *
 * Car extends Vehicle.
 * Implements the two abstract methods from Vehicle.
 * Cars have no extra category charge beyond the base daily rate.
 *
 * OOP Concept: SUPER keyword in constructor to initialise parent fields.
 * OOP Concept: METHOD OVERRIDING – getVehicleCategory and calculateCategoryCharge.
 */
@Entity
@DiscriminatorValue("CAR")
public class Car extends Vehicle {

    @Column(name = "number_of_doors")
    private int numberOfDoors;

    // ===== Constructors =====
    public Car() {
        super(); // OOP Concept: SUPER keyword
    }

    public Car(String brand, String model, String registrationNumber,
               BigDecimal pricePerDay, BigDecimal securityDeposit, int numberOfDoors) {
        super(brand, model, registrationNumber, pricePerDay, securityDeposit);
        this.numberOfDoors = numberOfDoors;
    }

    /** OOP Concept: METHOD OVERRIDING */
    @Override
    public String getVehicleCategory() {
        return "Car";
    }

    /**
     * OOP Concept: METHOD OVERRIDING
     * Cars carry no category surcharge – standard daily rate only.
     */
    @Override
    public BigDecimal calculateCategoryCharge(int rentalDays) {
        return BigDecimal.ZERO; // No surcharge for standard cars
    }

    public int getNumberOfDoors() { return numberOfDoors; }
    public void setNumberOfDoors(int numberOfDoors) { this.numberOfDoors = numberOfDoors; }
}
