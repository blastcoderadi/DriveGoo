# DriveGo – PowerPoint Presentation & Viva Guide

> **Course:** Java Programming & Object-Oriented Programming (OOP)  
> **Institution:** SRM University AP  
> **Project:** DriveGo – Vehicle Rental Management System  
> **Team:** 6 Members (AI & ML Specialization)  

---

## Slide 1: Title Slide
* **Title:** DriveGo – Vehicle Rental Management System (VRMS)
* **Subtitle:** An Enterprise-Grade Web Application Demonstrating Core Object-Oriented Programming in Java
* **Tech Stack:** Java 17, Spring Boot 3.3.5, Hibernate/JPA, Spring Security, MySQL/H2, Thymeleaf, Bootstrap 5
* **Team Details:** Team of 6, 2nd Semester B.Tech CSE (AI & ML), SRM University AP

---

## Slide 2: Project Overview & Architecture
* **What is DriveGo?**
  A production-ready vehicle rental web platform featuring a 3-step customer booking lifecycle, itemized transparent pricing, simulated multi-channel payments, and a real-time fleet operations admin portal.
* **Architecture:**
  * **Presentation Layer:** Thymeleaf Server-Side Templates + Bootstrap 5 (Responsive, WCAG AA contrast)
  * **Controller Layer:** Spring MVC REST & View Controllers (`VehicleController`, `BookingController`, `AdminController`, `AuthController`)
  * **Service Layer:** Pure Java Business Logic (`PricingService`, `BookingService`, `UserService`, `OtpService`)
  * **Persistence Layer:** Spring Data JPA + Hibernate (Single-Table Inheritance)
  * **Database:** MySQL 8 (Production) / In-Memory H2 (Development & Viva Demonstration)

---

## Slide 3: OOP Concepts Mapping Summary

| OOP Concept | Implementation Class | Real-World Purpose |
|---|---|---|
| **Encapsulation** | `Vehicle.java`, `User.java`, `PaymentResult.java` | Protect internal state; validate pricing/deposits strictly in mutators |
| **Abstraction** | `abstract class Vehicle`, `abstract class User` | Prevent invalid generic instances; enforce required subclass behavior |
| **Inheritance** | `Car`, `Bike`, `SUV` extend `Vehicle` | Code reuse of vehicle attributes; domain specialization |
| **Polymorphism** | `PaymentMethod` (`UPI`, `Card`, `Cash`) | Dynamic runtime selection of payment algorithms (Strategy Pattern) |
| **Method Overloading** | `PricingService.calculatePrice(...)` | 3 distinct signatures for flexible duration/discount calculations |
| **Method Overriding** | Subclasses override `calculateCategoryCharge()` | Different surcharge formulas for Cars (cleaning), Bikes (helmet), SUVs (4WD) |
| **static & final** | `TAX_RATE = 0.05`, `MAX_RENTAL_DAYS = 30` | Immutable business constants; class-level booking code generator |
| **Custom Exceptions** | `VehicleNotAvailableException`, `GlobalExceptionHandler` | Graceful domain error handling; zero user-facing stack traces |
| **Concurrency Control** | `@Lock(LockModeType.PESSIMISTIC_WRITE)` | Database-level row locking to prevent overlapping double bookings |

---

## Slide 4: Concept 1 – Encapsulation

### Bullet Points for Slide:
* Bundles state and methods together while hiding internal details using `private` fields.
* Setters act as domain guards, rejecting invalid data before it reaches the database.
* Immutable objects like `PaymentResult` use `private final` fields with no setters.

### Java Source Code:
```java
package com.drivego.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

public abstract class Vehicle {

    // 1. Data Hiding: Strictly private fields
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal securityDeposit;

    // 2. Encapsulation Mutator: Guarding domain rules with validation
    public void setPricePerDay(BigDecimal pricePerDay) {
        if (pricePerDay == null || pricePerDay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Daily price must be strictly positive.");
        }
        this.pricePerDay = pricePerDay;
    }

    public void setSecurityDeposit(BigDecimal securityDeposit) {
        if (securityDeposit == null || securityDeposit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Security deposit cannot be negative.");
        }
        this.securityDeposit = securityDeposit;
    }

    // 3. Encapsulation Accessors
    public BigDecimal getPricePerDay() { return pricePerDay; }
    public BigDecimal getSecurityDeposit() { return securityDeposit; }
    public String getBrand() { return brand; }
}
```

---

## Slide 5: Concept 2 – Abstraction

### Bullet Points for Slide:
* `abstract class Vehicle` hides generic implementation and cannot be instantiated (`new Vehicle()` is forbidden).
* Abstract methods define a mandatory contract that every vehicle category must fulfill.
* Callers interact with the abstract `Vehicle` reference without needing to know concrete vehicle mechanics.

### Java Source Code:
```java
package com.drivego.model;

import java.math.BigDecimal;

/**
 * OOP CONCEPT: ABSTRACTION
 * Generic vehicles cannot exist in real life – a rental is always a Car, Bike, or SUV.
 * Abstract methods force every category to define its own classification and fees.
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "vehicle_type")
public abstract class Vehicle {

    // Abstract method: Forces subclasses to return category label
    public abstract String getVehicleCategory();

    // Abstract method: Forces subclasses to calculate specialized fees
    public abstract BigDecimal calculateCategoryCharge(int rentalDays);
}
```

---

## Slide 6: Concept 3 – Inheritance & `super` Keyword

### Bullet Points for Slide:
* `Car`, `Bike`, and `SUV` inherit common attributes (`brand`, `model`, `pricePerDay`, `seats`) from `Vehicle`.
* Constructor chaining via `super(...)` passes shared parameters to the superclass constructor.
* Subclasses extend the parent with domain-specific fields (`numberOfDoors` in `Car`, `bikeType` in `Bike`, `fourWheelDrive` in `SUV`).

### Java Source Code:
```java
package com.drivego.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

/**
 * OOP CONCEPT: INHERITANCE
 * 'Car' IS-A 'Vehicle'. Inherits all properties from Vehicle.
 */
@Entity
@DiscriminatorValue("CAR")
public class Car extends Vehicle {

    private Integer numberOfDoors;

    public Car() {
        super(); // Constructor chaining
    }

    public Car(String brand, String model, String registrationNumber,
               BigDecimal pricePerDay, BigDecimal securityDeposit, Integer numberOfDoors) {
        // OOP: 'super' invokes parent class constructor
        super(brand, model, registrationNumber, pricePerDay, securityDeposit);
        this.numberOfDoors = numberOfDoors;
    }

    // OOP: Method Overriding – Custom charge logic for cars
    @Override
    public String getVehicleCategory() { return "Car"; }

    @Override
    public BigDecimal calculateCategoryCharge(int rentalDays) {
        return new BigDecimal("250.00"); // Standard sanitization and maintenance charge
    }
}
```

---

## Slide 7: Concept 4 – Polymorphism & Interfaces (Strategy Pattern)

### Bullet Points for Slide:
* `PaymentMethod` interface provides loose coupling between booking checkout and payment processors.
* Concrete classes `UPIPayment`, `CardPayment`, and `CashPayment` implement the interface.
* At runtime, `BookingService` delegates payment through a `PaymentMethod` reference without knowing the specific processor.

### Java Source Code:
```java
package com.drivego.payment;

import java.math.BigDecimal;

// 1. Interface defining payment capability
public interface PaymentMethod {
    PaymentResult pay(BigDecimal amount);
    String getMethodName();
}
```

```java
// 2. Concrete Polymorphic Implementations
@Component("UPI")
public class UPIPayment implements PaymentMethod {
    @Override
    public PaymentResult pay(BigDecimal amount) {
        String ref = "UPI-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentResult.success(ref, "Simulated UPI transfer of ₹" + amount + " successful.");
    }
    @Override public String getMethodName() { return "UPI"; }
}

@Component("CARD")
public class CardPayment implements PaymentMethod {
    @Override
    public PaymentResult pay(BigDecimal amount) {
        String ref = "CARD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return PaymentResult.success(ref, "Simulated Card transaction of ₹" + amount + " authorized.");
    }
    @Override public String getMethodName() { return "Card"; }
}
```

---

## Slide 8: Concept 5 – Method Overloading & `static final`

### Bullet Points for Slide:
* **Compile-time Polymorphism (Overloading):** `calculatePrice(...)` has 3 distinct method signatures.
* **`static final` Constants:** Class-level immutable values (`TAX_RATE`, `MAX_RENTAL_DAYS`).
* **Financial Accuracy:** All monetary computations use `BigDecimal` to avoid IEEE 754 floating-point rounding errors.

### Java Source Code:
```java
package com.drivego.service.impl;

import com.drivego.dto.PriceBreakdownDTO;
import com.drivego.model.Vehicle;
import com.drivego.service.PricingService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class PricingServiceImpl implements PricingService {

    // 1. static final: Immutable business constants
    public static final BigDecimal TAX_RATE = new BigDecimal("0.05"); // 5% GST
    public static final int MAX_RENTAL_DAYS = 30;

    // 2. Method Overloading (Signature 1: Dates)
    @Override
    public PriceBreakdownDTO calculatePrice(Vehicle vehicle, LocalDate pickupDate, LocalDate returnDate) {
        int days = (int) ChronoUnit.DAYS.between(pickupDate, returnDate);
        return calculatePrice(vehicle, days, BigDecimal.ZERO);
    }

    // 3. Method Overloading (Signature 2: Days)
    @Override
    public PriceBreakdownDTO calculatePrice(Vehicle vehicle, int rentalDays) {
        return calculatePrice(vehicle, rentalDays, BigDecimal.ZERO);
    }

    // 4. Method Overloading (Signature 3: Days + Discount)
    @Override
    public PriceBreakdownDTO calculatePrice(Vehicle vehicle, int rentalDays, BigDecimal discount) {
        BigDecimal baseRent = vehicle.getPricePerDay().multiply(BigDecimal.valueOf(rentalDays));
        BigDecimal categoryCharge = vehicle.calculateCategoryCharge(rentalDays); // Polymorphic call
        BigDecimal taxable = baseRent.add(categoryCharge);
        BigDecimal tax = taxable.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        BigDecimal deposit = vehicle.getSecurityDeposit();
        BigDecimal total = taxable.add(tax).add(deposit).subtract(discount);

        return new PriceBreakdownDTO(vehicle.getDisplayName(), vehicle.getVehicleCategory(),
                vehicle.getPricePerDay(), rentalDays, baseRent, categoryCharge, tax, deposit, total);
    }
}
```

---

## Slide 9: Concept 6 – Concurrency Safety & Pessimistic Locking

### Bullet Points for Slide:
* **The Overbooking Problem:** Two concurrent requests attempt to reserve the same vehicle on overlapping dates.
* **Solution:** Database row-level write lock (`@Lock(LockModeType.PESSIMISTIC_WRITE)`).
* **Mathematical Overlap Condition:**
  $$\text{Overlap} \iff (\text{newPickup} < \text{existingReturn}) \land (\text{newReturn} > \text{existingPickup})$$

### Java Source Code:
```java
package com.drivego.repository;

import com.drivego.model.Booking;
import org.springframework.data.jpa.repository.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    /**
     * CONCURRENCY CONTROL: Pessimistic Write Lock (SELECT ... FOR UPDATE)
     * Serializes concurrent booking attempts inside a @Transactional boundary.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.vehicle.id = :vehicleId " +
           "AND b.status IN (com.drivego.model.Booking.BookingStatus.PENDING, " +
           "                 com.drivego.model.Booking.BookingStatus.CONFIRMED, " +
           "                 com.drivego.model.Booking.BookingStatus.ACTIVE) " +
           "AND b.pickupDate < :returnDate " +
           "AND b.returnDate > :pickupDate")
    long countOverlapping(
            @Param("vehicleId") Long vehicleId,
            @Param("pickupDate") LocalDate pickupDate,
            @Param("returnDate") LocalDate returnDate);
}
```

---

## Slide 10: Concept 7 – Custom Exceptions & `@ControllerAdvice`

### Bullet Points for Slide:
* Extends `RuntimeException` for unchecked domain validation errors.
* Centralized exception handling via Spring `@ControllerAdvice`.
* Guarantees zero stack traces or internal technical leaks to the user.

### Java Source Code:
```java
package com.drivego.exception;

// Custom Domain Exception
public class VehicleNotAvailableException extends RuntimeException {
    public VehicleNotAvailableException(String message) {
        super(message);
    }
}
```

```java
package com.drivego.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

// Centralized Exception Interceptor
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VehicleNotAvailableException.class)
    public String handleUnavailable(VehicleNotAvailableException ex, Model model) {
        model.addAttribute("errorTitle", "Vehicle Unavailable");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/friendly"; // Clean, friendly error screen
    }
}
```

---

## Slide 11: Automated Verification & Unit Tests (JUnit 5)

### Test Suite Execution Summary:
* Total Tests: **9 Passed, 0 Failures, 0 Errors**
* Command: `mvn test`

| Test Case | Method | Validates |
|---|---|---|
| **TC01** | `testCalculatePriceForCar()` | Base rent, category surcharge, 5% GST, and deposit calculation |
| **TC02** | `testPolymorphicCategorySurcharge()` | Polymorphic difference between Bike helmet fee and SUV surcharge |
| **TC03** | `testPastPickupDateRejected()` | Date validation preventing past pickup timestamps |
| **TC04** | `testReturnBeforePickupRejected()` | Duration validation rejecting negative trip lengths |
| **TC05** | `testBookingOverlapRules()` | **Exact overlap rule:** 10–13 booked; 12–15 rejected; 14–17 allowed |
| **TC06** | `testCancelEligibleBooking()` | Customer cancellation transitions state to `CANCELLED` |
| **TC07** | `testPaymentMethodPolymorphism()` | Polymorphic execution of UPI, Card, and Cash implementations |
| **TC08** | `testLateReturnFeeCalculation()` | Penalty computation ($1.5\times$ daily rate $\times$ late days) |
| **TC09** | `testOtpWrongAttemptLimit()` | Security brute-force lockout after 5 incorrect OTP attempts |

---

## Slide 12: Live Demonstration Flow

1. **Customer Experience:**
   * Open `http://localhost:8080/`
   * Browse vehicle cards with real-time specs (Car, Bike, SUV).
   * 3-Step Booking: Select dates $\to$ Review itemized price breakdown $\to$ Confirm & generate tax invoice (`INV-2026-XXXXXX`).
   * Customer Dashboard: View booking code (`DG-XXXXXXXX`) and booking status.
2. **Admin Operations Portal:**
   * Open `http://localhost:8080/auth/demo-admin`
   * Real-time metrics: Fleet size, total revenue, pending reservations.
   * Manage Fleet: Add/Edit vehicles, toggle active status.
   * Manage Bookings: Approve pending reservations, complete trips with late fee penalty calculation.

---

## Slide 13: Conclusion & Learning Outcomes

* **Clean OOP Design:** Applied real-world abstraction, single-table inheritance, and the Strategy pattern without artificial or unused code.
* **Robust Concurrency:** Used pessimistic database locking to solve race conditions in multi-user booking scenarios.
* **Separation of Concerns:** Controllers handle requests, Services enforce rules, Repositories handle data access, and DTOs move data securely.
* **Industry Standards:** Maven dependency management, Spring Security role-based access, automated JUnit 5 tests, and responsive UI design.
