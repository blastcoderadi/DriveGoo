package com.drivego.repository;

import com.drivego.model.Booking;
import com.drivego.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserOrderByCreatedAtDesc(User user);

    Optional<Booking> findByBookingCode(String bookingCode);

    List<Booking> findByStatusOrderByCreatedAtDesc(Booking.BookingStatus status);

    List<Booking> findAllByOrderByCreatedAtDesc();

    /**
     * Overlap check: conflict if newPickup <= existing.returnDate
     * AND newReturn >= existing.pickupDate (and status is active).
     * Uses PESSIMISTIC_WRITE lock to prevent simultaneous double-booking.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.vehicle.id = :vehicleId " +
           "AND b.status IN (com.drivego.model.Booking.BookingStatus.PENDING, com.drivego.model.Booking.BookingStatus.CONFIRMED, com.drivego.model.Booking.BookingStatus.ACTIVE) " +
           "AND b.pickupDate < :returnDate " +
           "AND b.returnDate > :pickupDate")
    long countOverlapping(
            @Param("vehicleId") Long vehicleId,
            @Param("pickupDate") LocalDate pickupDate,
            @Param("returnDate") LocalDate returnDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.vehicle.id = :vehicleId " +
           "AND b.status IN (com.drivego.model.Booking.BookingStatus.PENDING, com.drivego.model.Booking.BookingStatus.CONFIRMED, com.drivego.model.Booking.BookingStatus.ACTIVE) " +
           "AND b.id != :excludeId " +
           "AND b.pickupDate < :returnDate " +
           "AND b.returnDate > :pickupDate")
    long countOverlappingExcluding(
            @Param("vehicleId") Long vehicleId,
            @Param("pickupDate") LocalDate pickupDate,
            @Param("returnDate") LocalDate returnDate,
            @Param("excludeId") Long excludeId);

    @Query("SELECT COALESCE(SUM(b.totalAmount),0) FROM Booking b WHERE b.status IN (com.drivego.model.Booking.BookingStatus.CONFIRMED, com.drivego.model.Booking.BookingStatus.ACTIVE, com.drivego.model.Booking.BookingStatus.COMPLETED)")
    BigDecimal getTotalRevenue();

    long countByStatus(Booking.BookingStatus status);

    long countByUser(User user);
}

