package com.drivego.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OOP Concept: ABSTRACTION + INHERITANCE (Abstract JPA Entity)
 *
 * Vehicle is an abstract class – you cannot instantiate it directly.
 * Car, Bike, and SUV are the concrete subclasses.
 *
 * Uses SINGLE_TABLE inheritance: all vehicle types share the `vehicles` table.
 * The `vehicle_type` column holds "CAR", "BIKE", or "SUV".
 *
 * Abstract methods force subclasses to implement category-specific logic:
 *   - getVehicleCategory()       : return the category label
 *   - calculateCategoryCharge()  : return extra charge for this vehicle type
 *
 * OOP Concept: ENCAPSULATION – private fields with validated setters.
 * OOP Concept: STATIC FINAL – shared constants.
 */
@Entity
@Table(name = "vehicles", indexes = {
    @Index(name = "idx_vehicle_type", columnList = "vehicle_type"),
    @Index(name = "idx_vehicle_price", columnList = "price_per_day")
})
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicle_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Vehicle {

    // OOP Concept: STATIC FINAL constants (shared across all Vehicle types)
    public static final int MAX_RENTAL_DAYS = 30;
    public static final BigDecimal TAX_RATE = new BigDecimal("0.05"); // 5% GST

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_type", insertable = false, updatable = false)
    private String vehicleType;

    @NotBlank(message = "Brand is required")
    @Column(name = "brand", nullable = false)
    private String brand;

    @NotBlank(message = "Model is required")
    @Column(name = "model", nullable = false)
    private String model;

    @Column(name = "registration_number", unique = true)
    private String registrationNumber;

    @NotNull(message = "Price per day is required")
    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @NotNull(message = "Security deposit is required")
    @Column(name = "security_deposit", nullable = false, precision = 10, scale = 2)
    private BigDecimal securityDeposit;

    @Column(name = "seats")
    private Integer seats;

    @Column(name = "fuel_type")
    private String fuelType;

    @Column(name = "transmission")
    private String transmission;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ===== Constructors =====
    protected Vehicle() {}

    protected Vehicle(String brand, String model, String registrationNumber,
                      BigDecimal pricePerDay, BigDecimal securityDeposit) {
        setBrand(brand);
        setModel(model);
        this.registrationNumber = registrationNumber;
        setPricePerDay(pricePerDay);
        setSecurityDeposit(securityDeposit);
    }

    // ===== Abstract Methods (Abstraction) =====
    /**
     * Returns the vehicle category label (e.g., "Car", "Bike", "SUV").
     * Every subclass MUST override this.
     */
    public abstract String getVehicleCategory();

    /**
     * Returns the category-specific extra charge for a given number of rental days.
     * Car = ₹0 extra, Bike = ₹200/day, SUV = 10% of base.
     * Every subclass MUST override this.
     */
    public abstract BigDecimal calculateCategoryCharge(int rentalDays);

    // ===== Getters and Setters (Encapsulation with validation) =====
    public Long getId() { return id; }
    public String getVehicleType() { return vehicleType; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) {
        if (brand == null || brand.isBlank())
            throw new IllegalArgumentException("Brand is required.");
        this.brand = brand.trim();
    }

    public String getModel() { return model; }
    public void setModel(String model) {
        if (model == null || model.isBlank())
            throw new IllegalArgumentException("Model is required.");
        this.model = model.trim();
    }

    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(BigDecimal pricePerDay) {
        if (pricePerDay == null || pricePerDay.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Price per day must be positive.");
        this.pricePerDay = pricePerDay;
    }

    public BigDecimal getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(BigDecimal securityDeposit) {
        if (securityDeposit != null && securityDeposit.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Security deposit cannot be negative.");
        this.securityDeposit = securityDeposit;
    }

    public Integer getSeats() { return seats; }
    public void setSeats(Integer seats) { this.seats = seats; }

    public String getFuelType() { return fuelType; }
    public void setFuelType(String fuelType) { this.fuelType = fuelType; }

    public String getTransmission() { return transmission; }
    public void setTransmission(String transmission) { this.transmission = transmission; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    // Display name helper
    public String getDisplayName() { return brand + " " + model; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicle v)) return false;
        return id != null && id.equals(v.id);
    }

    @Override
    public int hashCode() { return getClass().hashCode(); }

    @Override
    public String toString() {
        return getVehicleCategory() + " | " + brand + " " + model +
               " | ₹" + pricePerDay + "/day";
    }
}
