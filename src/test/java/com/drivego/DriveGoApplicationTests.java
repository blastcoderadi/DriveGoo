package com.drivego;

import com.drivego.dto.BookingRequestDTO;
import com.drivego.dto.PriceBreakdownDTO;
import com.drivego.exception.InvalidRentalDateException;
import com.drivego.exception.VehicleNotAvailableException;
import com.drivego.model.*;
import com.drivego.payment.*;
import com.drivego.repository.BookingRepository;
import com.drivego.repository.UserRepository;
import com.drivego.repository.VehicleRepository;
import com.drivego.service.BookingService;
import com.drivego.service.OtpService;
import com.drivego.service.PricingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class DriveGoApplicationTests {

    @Autowired
    private PricingService pricingService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private OtpService otpService;

    @Autowired
    private UPIPayment upiPayment;

    @Autowired
    private CardPayment cardPayment;

    @Autowired
    private CashPayment cashPayment;

    // =========================================================================
    // TEST 1: Price Calculation (Base + Category Surcharge + 5% Tax + Deposit)
    // =========================================================================
    @Test
    @DisplayName("TC01: Price calculation with 5% tax and deposit for Car")
    void testPriceCalculationForCar() {
        // Car: ₹2,500/day, deposit ₹5,000, 3 days
        // Base = 2500 * 3 = 7500
        // Surcharge = 0
        // Tax = 5% of 7500 = 375.00
        // Total = 7500 + 0 + 375 + 5000 = 12,875.00
        Car car = new Car("Maruti", "Swift", "DL-01-AA-1111",
                new BigDecimal("2500.00"), new BigDecimal("5000.00"), 4);

        PriceBreakdownDTO breakdown = pricingService.calculatePrice(car, 3);

        assertEquals(3, breakdown.getRentalDays());
        assertEquals(new BigDecimal("7500.00"), breakdown.getBaseRent());
        assertEquals(new BigDecimal("0.00"), breakdown.getCategoryCharge());
        assertEquals(new BigDecimal("375.00"), breakdown.getTaxAmount());
        assertEquals(new BigDecimal("5000.00"), breakdown.getSecurityDeposit());
        assertEquals(new BigDecimal("12875.00"), breakdown.getTotalAmount());
    }

    @Test
    @DisplayName("TC02: Polymorphic Category Surcharge - Bike helmet fee & SUV 10% premium")
    void testPolymorphicCategorySurcharges() {
        // Bike: ₹1,000/day, 2 days. Surcharge = ₹200/day * 2 = ₹400
        Bike bike = new Bike("Royal Enfield", "Hunter", "DL-02-BB-2222",
                new BigDecimal("1000.00"), new BigDecimal("3000.00"), "Cruiser");
        PriceBreakdownDTO bikePrice = pricingService.calculatePrice(bike, 2);
        assertEquals(new BigDecimal("2000.00"), bikePrice.getBaseRent());
        assertEquals(new BigDecimal("400.00"), bikePrice.getCategoryCharge());
        // Taxable = 2000 + 400 = 2400. Tax = 120. Total = 2400 + 120 + 3000 = 5520
        assertEquals(new BigDecimal("120.00"), bikePrice.getTaxAmount());
        assertEquals(new BigDecimal("5520.00"), bikePrice.getTotalAmount());

        // SUV: ₹5,000/day, 2 days. Base = 10,000. Surcharge = 10% of 10,000 = ₹1,000
        SUV suv = new SUV("Mahindra", "Thar", "DL-03-CC-3333",
                new BigDecimal("5000.00"), new BigDecimal("8000.00"), true);
        PriceBreakdownDTO suvPrice = pricingService.calculatePrice(suv, 2);
        assertEquals(new BigDecimal("10000.00"), suvPrice.getBaseRent());
        assertEquals(new BigDecimal("1000.00"), suvPrice.getCategoryCharge());
        // Taxable = 11,000. Tax = 550. Total = 11,000 + 550 + 8000 = 19,550
        assertEquals(new BigDecimal("550.00"), suvPrice.getTaxAmount());
        assertEquals(new BigDecimal("19550.00"), suvPrice.getTotalAmount());
    }

    // =========================================================================
    // TEST 2: Invalid Rental Dates Validation
    // =========================================================================
    @Test
    @DisplayName("TC03: Invalid rental dates - Past dates rejected")
    void testPastRentalDateRejected() {
        Customer customer = new Customer("Test User", "test@example.com", "9876543210", User.AuthProvider.PHONE);
        userRepository.save(customer);

        Car car = new Car("Honda", "City", "DL-04-DD-4444", new BigDecimal("3000"), new BigDecimal("5000"), 4);
        vehicleRepository.save(car);

        BookingRequestDTO dto = new BookingRequestDTO();
        dto.setVehicleId(car.getId());
        dto.setPickupDate(LocalDate.now().minusDays(2)); // In the past!
        dto.setReturnDate(LocalDate.now().plusDays(2));
        dto.setPickupLocation("Campus");
        dto.setPaymentMethod("CASH");

        assertThrows(InvalidRentalDateException.class, () -> bookingService.createBooking(dto, customer));
    }

    @Test
    @DisplayName("TC04: Invalid rental dates - Return before pickup rejected")
    void testReturnBeforePickupRejected() {
        Customer customer = new Customer("Test User", "test2@example.com", "9876543211", User.AuthProvider.PHONE);
        userRepository.save(customer);

        Car car = new Car("Honda", "Amaze", "DL-05-EE-5555", new BigDecimal("2000"), new BigDecimal("4000"), 4);
        vehicleRepository.save(car);

        BookingRequestDTO dto = new BookingRequestDTO();
        dto.setVehicleId(car.getId());
        dto.setPickupDate(LocalDate.now().plusDays(5));
        dto.setReturnDate(LocalDate.now().plusDays(3)); // Before pickup!
        dto.setPickupLocation("Campus");
        dto.setPaymentMethod("UPI");

        assertThrows(InvalidRentalDateException.class, () -> bookingService.createBooking(dto, customer));
    }

    // =========================================================================
    // TEST 3: Overlapping Bookings Logic (Exact 10–13 / 12–15 / 14–17 Case)
    // =========================================================================
    @Test
    @DisplayName("TC05: Overlapping Bookings Check - Booked 10-13 Nov, 12-15 rejected, 14-17 allowed")
    void testBookingOverlapRules() {
        Customer customer = new Customer("Aditya", "aditya@example.com", "9998887771", User.AuthProvider.PHONE);
        userRepository.save(customer);

        Car car = new Car("Hyundai", "Verna", "DL-06-FF-6666", new BigDecimal("3500"), new BigDecimal("5000"), 4);
        vehicleRepository.save(car);

        // Case 1: Initial booking for 10 Nov to 13 Nov (using future dates relative to now)
        LocalDate day10 = LocalDate.now().plusDays(10);
        LocalDate day13 = LocalDate.now().plusDays(13);
        LocalDate day12 = LocalDate.now().plusDays(12);
        LocalDate day15 = LocalDate.now().plusDays(15);
        LocalDate day14 = LocalDate.now().plusDays(14);
        LocalDate day17 = LocalDate.now().plusDays(17);

        BookingRequestDTO b1 = new BookingRequestDTO();
        b1.setVehicleId(car.getId());
        b1.setPickupDate(day10);
        b1.setReturnDate(day13);
        b1.setPickupLocation("Main Hub");
        b1.setPaymentMethod("UPI");

        Booking confirmedBooking = bookingService.createBooking(b1, customer);
        bookingService.confirmBooking(confirmedBooking.getId());
        assertEquals(Booking.BookingStatus.CONFIRMED, confirmedBooking.getStatus());

        // Case 2: Attempt overlapping booking for 12 Nov to 15 Nov -> MUST BE REJECTED
        BookingRequestDTO b2 = new BookingRequestDTO();
        b2.setVehicleId(car.getId());
        b2.setPickupDate(day12);
        b2.setReturnDate(day15);
        b2.setPickupLocation("Main Hub");
        b2.setPaymentMethod("UPI");

        assertThrows(VehicleNotAvailableException.class, () -> bookingService.createBooking(b2, customer),
                "Expected VehicleNotAvailableException when dates overlap 10-13 with 12-15");

        // Case 3: Attempt non-overlapping booking for 14 Nov to 17 Nov -> MUST BE ALLOWED
        BookingRequestDTO b3 = new BookingRequestDTO();
        b3.setVehicleId(car.getId());
        b3.setPickupDate(day14);
        b3.setReturnDate(day17);
        b3.setPickupLocation("Main Hub");
        b3.setPaymentMethod("UPI");

        Booking allowedBooking = bookingService.createBooking(b3, customer);
        assertNotNull(allowedBooking);
        assertNotNull(allowedBooking.getBookingCode());
        assertEquals(Booking.BookingStatus.PENDING, allowedBooking.getStatus());
    }

    // =========================================================================
    // TEST 4: Booking Cancellation
    // =========================================================================
    @Test
    @DisplayName("TC06: Customer can cancel future booking; status changes to CANCELLED")
    void testCancelEligibleBooking() {
        Customer customer = new Customer("Rohan", "rohan@example.com", "9112233445", User.AuthProvider.PHONE);
        userRepository.save(customer);

        Car car = new Car("Tata", "Nexon", "DL-07-GG-7777", new BigDecimal("2800"), new BigDecimal("5000"), 4);
        vehicleRepository.save(car);

        BookingRequestDTO dto = new BookingRequestDTO();
        dto.setVehicleId(car.getId());
        dto.setPickupDate(LocalDate.now().plusDays(3));
        dto.setReturnDate(LocalDate.now().plusDays(6));
        dto.setPickupLocation("Airport");
        dto.setPaymentMethod("CASH");

        Booking booking = bookingService.createBooking(dto, customer);
        assertTrue(booking.isCancellable());

        bookingService.cancelBooking(booking.getId(), customer);

        Booking reloaded = bookingService.findById(booking.getId()).orElseThrow();
        assertEquals(Booking.BookingStatus.CANCELLED, reloaded.getStatus());
    }

    // =========================================================================
    // TEST 5: Payment Method Polymorphism (UPI, Card, Cash)
    // =========================================================================
    @Test
    @DisplayName("TC07: Polymorphic PaymentMethod implementations execute properly")
    void testPaymentPolymorphism() {
        BigDecimal testAmount = new BigDecimal("7500.00");

        // Reference of interface PaymentMethod holding UPIPayment
        PaymentMethod pm1 = upiPayment;
        PaymentResult r1 = pm1.pay(testAmount);
        assertTrue(r1.isSuccessful());
        assertEquals("UPI", pm1.getMethodName());
        assertTrue(r1.getTransactionRef().startsWith("UPI-"));

        // Reference of interface PaymentMethod holding CardPayment
        PaymentMethod pm2 = cardPayment;
        PaymentResult r2 = pm2.pay(testAmount);
        assertTrue(r2.isSuccessful());
        assertEquals("Credit/Debit Card", pm2.getMethodName());
        assertTrue(r2.getTransactionRef().startsWith("CARD-"));

        // Reference of interface PaymentMethod holding CashPayment
        PaymentMethod pm3 = cashPayment;
        PaymentResult r3 = pm3.pay(testAmount);
        assertTrue(r3.isSuccessful());
        assertEquals("Cash on Pickup", pm3.getMethodName());
        assertTrue(r3.getTransactionRef().startsWith("CASH-"));
    }

    // =========================================================================
    // TEST 6: Late Return Fee Calculation
    // =========================================================================
    @Test
    @DisplayName("TC08: Late return charge calculated as lateDays x dailyPrice x 1.5")
    void testLateReturnFee() {
        Customer customer = new Customer("Late User", "late@example.com", "9876500000", User.AuthProvider.PHONE);
        userRepository.save(customer);

        Car car = new Car("Kia", "Seltos", "DL-08-HH-8888", new BigDecimal("4000.00"), new BigDecimal("6000.00"), 4);
        vehicleRepository.save(car);

        BookingRequestDTO dto = new BookingRequestDTO();
        dto.setVehicleId(car.getId());
        dto.setPickupDate(LocalDate.now().plusDays(1));
        dto.setReturnDate(LocalDate.now().plusDays(2));
        dto.setPickupLocation("Campus");
        dto.setPaymentMethod("UPI");

        Booking booking = bookingService.createBooking(dto, customer);
        BigDecimal initialTotal = booking.getTotalAmount();

        // Complete with 2 late days: 2 * 4000 * 1.5 = ₹12,000 late fee
        bookingService.completeBooking(booking.getId(), 2);

        Booking completed = bookingService.findById(booking.getId()).orElseThrow();
        assertEquals(Booking.BookingStatus.COMPLETED, completed.getStatus());
        assertEquals(new BigDecimal("12000.00"), completed.getLateFee());
        assertEquals(initialTotal.add(new BigDecimal("12000.00")), completed.getTotalAmount());
    }

    // =========================================================================
    // TEST 7: Phone OTP Authentication (Generation, Expiry, Wrong Attempt Limit)
    // =========================================================================
    @Test
    @DisplayName("TC09: OTP Wrong Attempt Limit (5 tries) blocks user")
    void testOtpAttemptLimitAndBlock() {
        String testPhone = "9876543219";
        otpService.sendOtp(testPhone);

        // 4 wrong attempts
        for (int i = 1; i <= 4; i++) {
            OtpService.OtpVerifyResult result = otpService.verifyOtp(testPhone, "000000");
            assertFalse(result.success());
            assertEquals(5 - i, result.triesLeft());
        }

        // 5th wrong attempt -> blocked
        OtpService.OtpVerifyResult fifthResult = otpService.verifyOtp(testPhone, "000000");
        assertFalse(fifthResult.success());
        assertTrue(fifthResult.message().toLowerCase().contains("blocked"));
    }
}
