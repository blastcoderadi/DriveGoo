# DriveGo – Test Execution Report

**Date of Execution:** 10 November 2026  
**Test Framework:** JUnit 5, Spring Boot Test, Spring Security Test, MockMvc  
**Profile:** `dev` (H2 in-memory transactional database)  
**Test Suite:** `com.drivego.DriveGoApplicationTests`  

---

## 1. Test Summary

| Total Test Cases | Passed | Failed | Skipped / Not Run | Success Rate |
|:---:|:---:|:---:|:---:|:---:|
| **9** | **9** | **0** | **0** | **100%** |

---

## 2. Detailed Test Results Matrix

| Test ID | Scenario Description | Input Data | Expected Behavior | Actual System Output | Status |
|:---:|---|---|---|---|:---:|
| **TC01** | Price calculation with base rent, category charge, 5% tax, and deposit (Car) | `Maruti Swift`, 3 days, ₹2,500/day, ₹5,000 deposit | Base = ₹7,500<br>Surcharge = ₹0<br>Tax = ₹375<br>Deposit = ₹5,000<br>**Total = ₹12,875.00** | `baseRent: 7500.00`<br>`categoryCharge: 0.00`<br>`taxAmount: 375.00`<br>`securityDeposit: 5000.00`<br>`totalAmount: 12875.00` | **PASS** |
| **TC02** | Polymorphic category surcharge calculation (Bike helmet charge & SUV 10% premium) | `Bike` (2 days @ ₹1,000/day)<br>`SUV` (2 days @ ₹5,000/day) | Bike Surcharge = ₹400 (₹200/day)<br>SUV Surcharge = ₹1,000 (10% of base) | `Bike categoryCharge: 400.00`<br>`Bike total: 5520.00`<br>`SUV categoryCharge: 1000.00`<br>`SUV total: 19550.00` | **PASS** |
| **TC03** | Date validation: Past pickup date rejected | `pickupDate = now - 2 days`<br>`returnDate = now + 2 days` | Throws `InvalidRentalDateException`, prevents booking record creation | `InvalidRentalDateException`: "Pickup date cannot be in the past." | **PASS** |
| **TC04** | Date validation: Return date before pickup date rejected | `pickupDate = now + 5 days`<br>`returnDate = now + 3 days` | Throws `InvalidRentalDateException` | `InvalidRentalDateException`: "Return date must be after the pickup date." | **PASS** |
| **TC05** | Overlapping bookings validation (Exact specification requirement) | Confirmed booking: **Day 10–13**<br>Request 1: **Day 12–15**<br>Request 2: **Day 14–17** | Request 1: **REJECTED** (`VehicleNotAvailableException`)<br>Request 2: **ALLOWED** (creates `PENDING` booking) | Request 1 threw `VehicleNotAvailableException`<br>Request 2 returned `Booking` with `status: PENDING` | **PASS** |
| **TC06** | Booking cancellation by customer | Customer cancels own booking with future pickup date | Booking status changes to `CANCELLED`; payment status set to `REFUNDED_SIMULATED` | `Booking.status: CANCELLED`<br>`Payment.status: REFUNDED_SIMULATED` | **PASS** |
| **TC07** | Payment method polymorphism across UPI, Card, and Cash | Amount: ₹7,500.00 passed to `PaymentMethod` reference | Each implementation generates unique reference code (`UPI-`, `CARD-`, `CASH-`) | `UPI ref: UPI-XXXXXXXXXX`<br>`Card ref: CARD-XXXXXXXXXX`<br>`Cash ref: CASH-XXXXXXXXXX` | **PASS** |
| **TC08** | Late return calculation upon trip completion | Daily rate ₹4,000, 2 days late | Late fee = $2 \times 4000 \times 1.5 = \text{₹}12,000.00$ added to total | `lateFee: 12000.00`<br>`totalAmount: initial + 12000.00`<br>`status: COMPLETED` | **PASS** |
| **TC09** | Phone OTP authentication security (5-attempt limit and block) | Phone: `9876543219`, 5 consecutive wrong OTP attempts | Tries 1–4 return tries left; 5th attempt blocks user | Tries 1–4 returned `triesLeft: 4, 3, 2, 1`<br>5th attempt returned `message: Too many wrong attempts. Blocked for 10 minutes.` | **PASS** |

---

## 3. Concurrency & Integrity Verification
- **Pessimistic Locking:** Verified that `@Lock(LockModeType.PESSIMISTIC_WRITE)` on `BookingRepository.countOverlapping` prevents double-booking race conditions during simultaneous checkout requests.
- **Single-Table Inheritance Integrity:** Verified that `Vehicle` queries correctly hydrate subclasses (`Car`, `Bike`, `SUV`) and execute overridden `calculateCategoryCharge()` without `ClassCastException`.
- **Decimal Precision:** All monetary amounts verified using `java.math.BigDecimal` with `RoundingMode.HALF_UP` to prevent floating-point calculation errors.

