package com.drivego.repository;

import com.drivego.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.util.List;

@Repository
public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByActiveTrue();

    @Query("SELECT v FROM Vehicle v WHERE v.active = true " +
           "AND (:category IS NULL OR :category = '' OR UPPER(v.vehicleType) = UPPER(:category)) " +
           "AND (:minPrice IS NULL OR v.pricePerDay >= :minPrice) " +
           "AND (:maxPrice IS NULL OR v.pricePerDay <= :maxPrice) " +
           "AND (:search IS NULL OR :search = '' OR LOWER(v.brand) LIKE LOWER(CONCAT('%',:search,'%')) " +
           "     OR LOWER(v.model) LIKE LOWER(CONCAT('%',:search,'%')))")
    List<Vehicle> searchVehicles(
            @Param("search") String search,
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT DISTINCT v.vehicleType FROM Vehicle v ORDER BY 1")
    List<String> findDistinctTypes();

    @Query("SELECT COUNT(b) > 0 FROM Booking b WHERE b.vehicle.id = :vehicleId " +
           "AND b.status IN ('PENDING','CONFIRMED','ACTIVE')")
    boolean hasActiveBookings(@Param("vehicleId") Long vehicleId);
}

