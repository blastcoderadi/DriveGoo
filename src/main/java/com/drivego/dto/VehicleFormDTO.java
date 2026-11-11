package com.drivego.dto;

import jakarta.validation.constraints.*;

public class VehicleFormDTO {

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "Vehicle type is required")
    private String vehicleType; // CAR, BIKE, SUV

    private String registrationNumber;

    @NotNull(message = "Price per day is required")
    @DecimalMin(value = "1.0", message = "Price must be at least ₹1")
    private java.math.BigDecimal pricePerDay;

    @NotNull(message = "Security deposit is required")
    @DecimalMin(value = "0.0", message = "Deposit cannot be negative")
    private java.math.BigDecimal securityDeposit;

    private Integer seats;
    private String fuelType;
    private String transmission;
    private String description;
    private String imageUrl;
    private boolean active = true;

    // Vehicle-type specific
    private Integer numberOfDoors;    // Car
    private String bikeType;          // Bike
    private Boolean fourWheelDrive;   // SUV

    // Getters and Setters
    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getRegistrationNumber() { return registrationNumber; }
    public void setRegistrationNumber(String reg) { this.registrationNumber = reg; }
    public java.math.BigDecimal getPricePerDay() { return pricePerDay; }
    public void setPricePerDay(java.math.BigDecimal pricePerDay) { this.pricePerDay = pricePerDay; }
    public java.math.BigDecimal getSecurityDeposit() { return securityDeposit; }
    public void setSecurityDeposit(java.math.BigDecimal securityDeposit) { this.securityDeposit = securityDeposit; }
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
    public Integer getNumberOfDoors() { return numberOfDoors; }
    public void setNumberOfDoors(Integer numberOfDoors) { this.numberOfDoors = numberOfDoors; }
    public String getBikeType() { return bikeType; }
    public void setBikeType(String bikeType) { this.bikeType = bikeType; }
    public Boolean getFourWheelDrive() { return fourWheelDrive; }
    public void setFourWheelDrive(Boolean fourWheelDrive) { this.fourWheelDrive = fourWheelDrive; }
}

