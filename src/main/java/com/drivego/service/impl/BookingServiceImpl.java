package com.drivego.service.impl;

import com.drivego.dto.BookingRequestDTO;
import com.drivego.dto.PriceBreakdownDTO;
import com.drivego.exception.*;
import com.drivego.model.*;
import com.drivego.payment.*;
import com.drivego.repository.*;
import com.drivego.service.BookingService;
import com.drivego.service.PricingService;
import com.drivego.util.BookingCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * OOP Concept: INTERFACE IMPLEMENTATION + POLYMORPHISM + EXCEPTION HANDLING
 *
 * BookingServiceImpl orchestrates the entire booking flow:
 * date validation → availability check → price calculation →
 * booking creation → payment (via PaymentMethod interface) → invoice generation.
 *
 * POLYMORPHISM: Uses PaymentMethod interface so UPI/Card/Cash are interchangeable.
 * EXCEPTION HANDLING: Throws custom exceptions, never shows raw errors.
 */
@Service
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final VehicleRepository vehicleRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final PricingService pricingService;

    // OOP Concept: POLYMORPHISM – Map holds all PaymentMethod implementations
    private final Map<String, PaymentMethod> paymentMethods;

    public BookingServiceImpl(BookingRepository bookingRepository,
                               VehicleRepository vehicleRepository,
                               PaymentRepository paymentRepository,
                               InvoiceRepository invoiceRepository,
                               PricingService pricingService,
                               UPIPayment upiPayment,
                               CardPayment cardPayment,
                               CashPayment cashPayment) {
        this.bookingRepository = bookingRepository;
        this.vehicleRepository = vehicleRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
        this.pricingService = pricingService;
        // OOP Concept: POLYMORPHISM – all stored as PaymentMethod references
        this.paymentMethods = Map.of(
                "UPI", upiPayment,
                "CARD", cardPayment,
                "CASH", cashPayment
        );
    }

    @Override
    @Transactional
    public Booking createBooking(BookingRequestDTO dto, User user) {

        // === STEP 1: Validate Dates ===
        validateDates(dto.getPickupDate(), dto.getReturnDate());

        // === STEP 2: Find Vehicle ===
        Vehicle vehicle = vehicleRepository.findById(dto.getVehicleId())
                .orElseThrow(() -> new InvalidBookingException("Vehicle not found."));
        if (!vehicle.isActive()) {
            throw new VehicleNotAvailableException("This vehicle is not currently available.");
        }

        // === STEP 3: Availability Check (with DB lock to prevent double-booking) ===
        long conflicts = bookingRepository.countOverlapping(
                vehicle.getId(), dto.getPickupDate(), dto.getReturnDate());
        if (conflicts > 0) {
            throw new VehicleNotAvailableException(
                    vehicle.getDisplayName() + " is already booked for the selected dates. " +
                    "Please choose different dates.");
        }

        // === STEP 4: Price Calculation (always server-side, never trust browser) ===
        // OOP Concept: POLYMORPHISM – calculatePrice calls correct subclass method
        PriceBreakdownDTO price = pricingService.calculatePrice(
                vehicle, dto.getPickupDate(), dto.getReturnDate());

        // === STEP 5: Create Booking ===
        int days = (int) Math.max(1, ChronoUnit.DAYS.between(dto.getPickupDate(), dto.getReturnDate()));
        Booking booking = new Booking();
        booking.setBookingCode(BookingCodeGenerator.generate());
        booking.setUser(user);
        booking.setVehicle(vehicle);
        booking.setPickupDate(dto.getPickupDate());
        booking.setReturnDate(dto.getReturnDate());
        booking.setRentalDays(days);
        booking.setPickupLocation(dto.getPickupLocation());
        booking.setDailyPriceSnapshot(vehicle.getPricePerDay());
        booking.setBaseRent(price.getBaseRent());
        booking.setCategoryCharge(price.getCategoryCharge());
        booking.setTaxAmount(price.getTaxAmount());
        booking.setSecurityDeposit(price.getSecurityDeposit());
        booking.setLateFee(BigDecimal.ZERO);
        booking.setTotalAmount(price.getTotalAmount());
        booking.setStatus(Booking.BookingStatus.PENDING);
        bookingRepository.save(booking);

        // === STEP 6: Simulated Payment ===
        // OOP Concept: POLYMORPHISM – resolve correct PaymentMethod at runtime
        String methodKey = dto.getPaymentMethod().toUpperCase();
        PaymentMethod pm = paymentMethods.getOrDefault(methodKey, paymentMethods.get("CASH"));
        PaymentResult result = pm.pay(price.getTotalAmount());

        if (!result.isSuccessful()) {
            throw new InvalidPaymentException("Payment failed: " + result.getMessage());
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(price.getTotalAmount());
        payment.setMethod(pm.getMethodName());
        payment.setTransactionRef(result.getTransactionRef());
        payment.setStatus(Payment.PaymentStatus.SIMULATED_PAID);
        payment.setPaidAt(LocalDateTime.now());
        paymentRepository.save(payment);

        // === STEP 7: Generate Invoice ===
        generateInvoice(booking, pm.getMethodName());

        return booking;
    }

    private void validateDates(LocalDate pickup, LocalDate returnDate) {
        if (pickup == null || returnDate == null) {
            throw new InvalidRentalDateException("Both pickup and return dates are required.");
        }
        if (pickup.isBefore(LocalDate.now())) {
            throw new InvalidRentalDateException("Pickup date cannot be in the past.");
        }
        if (!returnDate.isAfter(pickup)) {
            throw new InvalidRentalDateException("Return date must be after the pickup date.");
        }
        long days = ChronoUnit.DAYS.between(pickup, returnDate);
        if (days > Vehicle.MAX_RENTAL_DAYS) {
            throw new InvalidRentalDateException(
                    "Maximum rental period is " + Vehicle.MAX_RENTAL_DAYS + " days.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAvailable(Long vehicleId, LocalDate pickup, LocalDate returnDate) {
        return bookingRepository.countOverlapping(vehicleId, pickup, returnDate) == 0;
    }

    @Override
    public void cancelBooking(Long id, User user) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new InvalidBookingException("Booking not found."));
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new InvalidBookingException("You can only cancel your own bookings.");
        }
        if (!booking.isCancellable()) {
            throw new InvalidBookingException(
                    "This booking cannot be cancelled. Only PENDING or CONFIRMED bookings " +
                    "with a future pickup date are eligible.");
        }
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        // Mark payment as refunded (simulated)
        paymentRepository.findByBookingId(id).ifPresent(p -> {
            p.setStatus(Payment.PaymentStatus.REFUNDED_SIMULATED);
            paymentRepository.save(p);
        });
        bookingRepository.save(booking);
    }

    @Override
    public void confirmBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new InvalidBookingException("Booking not found."));
        // Re-check overlap at confirmation time
        long conflicts = bookingRepository.countOverlappingExcluding(
                booking.getVehicle().getId(),
                booking.getPickupDate(), booking.getReturnDate(), booking.getId());
        if (conflicts > 0) {
            throw new VehicleNotAvailableException(
                    "Cannot confirm: dates now conflict with another confirmed booking.");
        }
        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    @Override
    public void rejectBooking(Long id, String reason) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new InvalidBookingException("Booking not found."));
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancellationReason(reason);
        bookingRepository.save(booking);
    }

    @Override
    public void markActive(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new InvalidBookingException("Booking not found."));
        booking.setStatus(Booking.BookingStatus.ACTIVE);
        bookingRepository.save(booking);
    }

    @Override
    public void completeBooking(Long id, int lateDays) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new InvalidBookingException("Booking not found."));
        BigDecimal lateFee = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        if (lateDays > 0) {
            // Late fee = lateDays × dailyPrice × 1.5 (penalty)
            lateFee = booking.getDailyPriceSnapshot()
                    .multiply(new BigDecimal(lateDays))
                    .multiply(new BigDecimal("1.5"))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        booking.setLateFee(lateFee);
        booking.setTotalAmount(booking.getTotalAmount().add(lateFee).setScale(2, RoundingMode.HALF_UP));
        booking.setStatus(Booking.BookingStatus.COMPLETED);
        bookingRepository.save(booking);
    }

    private void generateInvoice(Booking booking, String paymentMethod) {
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(BookingCodeGenerator.generateInvoiceNumber());
        invoice.setBooking(booking);
        invoice.setCustomerName(booking.getUser().getFullName());
        invoice.setCustomerEmail(booking.getUser().getEmail());
        invoice.setCustomerPhone(booking.getUser().getPhone());
        invoice.setVehicleName(booking.getVehicle().getDisplayName());
        invoice.setVehicleCategory(booking.getVehicle().getVehicleCategory());
        invoice.setPickupDate(booking.getPickupDate());
        invoice.setReturnDate(booking.getReturnDate());
        invoice.setRentalDays(booking.getRentalDays());
        invoice.setDailyRate(booking.getDailyPriceSnapshot());
        invoice.setBaseRent(booking.getBaseRent());
        invoice.setCategoryCharge(booking.getCategoryCharge());
        invoice.setTaxAmount(booking.getTaxAmount());
        invoice.setSecurityDeposit(booking.getSecurityDeposit());
        invoice.setTotalAmount(booking.getTotalAmount());
        invoice.setPaymentMethod(paymentMethod);
        invoice.setPaymentStatus("SIMULATED_PAID");
        invoiceRepository.save(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findById(Long id) { return bookingRepository.findById(id); }

    @Override
    @Transactional(readOnly = true)
    public Optional<Booking> findByCode(String code) { return bookingRepository.findByBookingCode(code); }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findByUser(User user) { return bookingRepository.findByUserOrderByCreatedAtDesc(user); }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findAll() { return bookingRepository.findAllByOrderByCreatedAtDesc(); }

    @Override
    @Transactional(readOnly = true)
    public List<Booking> findByStatus(Booking.BookingStatus status) {
        return bookingRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(Booking.BookingStatus status) { return bookingRepository.countByStatus(status); }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalRevenue() {
        BigDecimal rev = bookingRepository.getTotalRevenue();
        return rev != null ? rev : BigDecimal.ZERO;
    }
}
