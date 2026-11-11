package com.drivego.service.impl;

import com.drivego.dto.VehicleFormDTO;
import com.drivego.model.*;
import com.drivego.repository.VehicleRepository;
import com.drivego.service.VehicleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * OOP Concept: INTERFACE IMPLEMENTATION + INHERITANCE usage (Car/Bike/SUV creation)
 */
@Service
@Transactional
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleServiceImpl(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public Vehicle save(VehicleFormDTO dto) {
        Vehicle v = createFromType(dto);
        return vehicleRepository.save(v);
    }

    @Override
    public Vehicle update(Long id, VehicleFormDTO dto) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + id));
        mapDtoToVehicle(v, dto);
        return vehicleRepository.save(v);
    }

    @Override
    public void delete(Long id) {
        if (vehicleRepository.hasActiveBookings(id)) {
            throw new IllegalStateException(
                    "This vehicle has active bookings and cannot be deleted. Cancel bookings first.");
        }
        vehicleRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Vehicle> findById(Long id) { return vehicleRepository.findById(id); }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> findAll() { return vehicleRepository.findAll(); }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> findAvailable() { return vehicleRepository.findByActiveTrue(); }

    @Override
    @Transactional(readOnly = true)
    public List<Vehicle> search(String query, String category,
                                BigDecimal minPrice, BigDecimal maxPrice) {
        String q = (query != null && !query.isBlank()) ? query.trim() : null;
        String cat = (category != null && !category.isBlank()) ? category : null;
        return vehicleRepository.searchVehicles(q, cat, minPrice, maxPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getCategories() {
        return Arrays.asList("Car", "Bike", "SUV");
    }

    @Override
    public void toggleActive(Long id) {
        Vehicle v = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + id));
        v.setActive(!v.isActive());
        vehicleRepository.save(v);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() { return vehicleRepository.count(); }

    // OOP Concept: POLYMORPHISM – creates the right Vehicle subclass based on type string
    private Vehicle createFromType(VehicleFormDTO dto) {
        Vehicle v = switch (dto.getVehicleType().toUpperCase()) {
            case "CAR" -> {
                Car car = new Car();
                if (dto.getNumberOfDoors() != null) car.setNumberOfDoors(dto.getNumberOfDoors());
                yield car;
            }
            case "BIKE" -> {
                Bike bike = new Bike();
                bike.setBikeType(dto.getBikeType());
                yield bike;
            }
            case "SUV" -> {
                SUV suv = new SUV();
                if (dto.getFourWheelDrive() != null) suv.setFourWheelDrive(dto.getFourWheelDrive());
                yield suv;
            }
            default -> throw new IllegalArgumentException("Unknown vehicle type: " + dto.getVehicleType());
        };
        mapDtoToVehicle(v, dto);
        return v;
    }

    private void mapDtoToVehicle(Vehicle v, VehicleFormDTO dto) {
        v.setBrand(dto.getBrand());
        v.setModel(dto.getModel());
        v.setRegistrationNumber(dto.getRegistrationNumber());
        v.setPricePerDay(dto.getPricePerDay());
        v.setSecurityDeposit(dto.getSecurityDeposit());
        v.setSeats(dto.getSeats());
        v.setFuelType(dto.getFuelType());
        v.setTransmission(dto.getTransmission());
        v.setDescription(dto.getDescription());
        v.setImageUrl(dto.getImageUrl());
        v.setActive(dto.isActive());
    }
}
