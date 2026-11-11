package com.drivego.service;

import com.drivego.dto.VehicleFormDTO;
import com.drivego.model.Vehicle;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface VehicleService {
    Vehicle save(VehicleFormDTO dto);
    Vehicle update(Long id, VehicleFormDTO dto);
    void delete(Long id);
    Optional<Vehicle> findById(Long id);
    List<Vehicle> findAll();
    List<Vehicle> findAvailable();
    List<Vehicle> search(String query, String category, BigDecimal minPrice, BigDecimal maxPrice);
    List<String> getCategories();
    void toggleActive(Long id);
    long count();
}
