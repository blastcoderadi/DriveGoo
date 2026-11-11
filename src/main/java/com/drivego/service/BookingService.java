package com.drivego.service;

import com.drivego.dto.BookingRequestDTO;
import com.drivego.model.Booking;
import com.drivego.model.User;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingService {
    Booking createBooking(BookingRequestDTO dto, User user);
    Optional<Booking> findById(Long id);
    Optional<Booking> findByCode(String code);
    List<Booking> findByUser(User user);
    List<Booking> findAll();
    List<Booking> findByStatus(Booking.BookingStatus status);
    void cancelBooking(Long id, User user);
    void confirmBooking(Long id);
    void rejectBooking(Long id, String reason);
    void markActive(Long id);
    void completeBooking(Long id, int lateDays);
    boolean isAvailable(Long vehicleId, LocalDate pickup, LocalDate returnDate);
    long countByStatus(Booking.BookingStatus status);
    java.math.BigDecimal getTotalRevenue();
}
