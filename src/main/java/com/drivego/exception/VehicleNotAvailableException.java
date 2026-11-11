package com.drivego.exception;
public class VehicleNotAvailableException extends RuntimeException {
    private final Long vehicleId;
    public VehicleNotAvailableException(String message) { super(message); this.vehicleId = null; }
    public VehicleNotAvailableException(String message, Long vehicleId) { super(message); this.vehicleId = vehicleId; }
    public Long getVehicleId() { return vehicleId; }
}

