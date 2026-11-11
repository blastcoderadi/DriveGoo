# DriveGo – Object-Oriented Programming (OOP) Implementation Guide

> **Course:** Java Programming & Object-Oriented Programming (OOP)  
> **Institution:** SRM University AP  
> **Student Specialization:** AI & ML  
> **Project:** DriveGo – Vehicle Rental Management System  

This document explains where and how each core Object-Oriented Programming (OOP) concept is practically implemented in the DriveGo codebase. Every concept was chosen to solve an authentic business or engineering requirement.

---

## 1. Encapsulation

**Definition:** Bundling data (attributes) and methods that operate on that data into a single unit (class), while restricting direct access to internal state using `private` access modifiers and providing controlled access via getters, setters, and business methods.

### Implementation in DriveGo:
- **`com.drivego.model.Vehicle`**
  - All attributes (`id`, `brand`, `model`, `pricePerDay`, `securityDeposit`, `active`) are strictly `private`.
  - Mutators enforce domain validation. For example:
    ```java
    public void setPricePerDay(BigDecimal pricePerDay) {
        if (pricePerDay == null || pricePerDay.compareTo(BigDecimal.ZERO) <= 0)
            throw new IllegalArgumentException("Price per day must be positive.");
        this.pricePerDay = pricePerDay;
    }
    ```
- **`com.drivego.model.User`**
  - Encapsulates `email`, `phone`, `authProvider`, and `fullName`.
  - Prevents setting blank names via setter validation.
- **`com.drivego.payment.PaymentResult`**
  - All fields are `private final`. State is immutable and cannot be modified after instantiation. Instantiation is controlled exclusively through static factory methods.

---

## 2. Abstraction

**Definition:** Hiding complex implementation details and exposing only essential characteristics and interfaces to the outside world.

### Implementation in DriveGo:
- **`com.drivego.model.Vehicle` (Abstract Entity Class)**
  - Marked with `public abstract class Vehicle`.
  - Cannot be instantiated directly (`new Vehicle()` is forbidden by the compiler).
  - Forces concrete subclasses (`Car`, `Bike`, `SUV`) to define category-specific behavior via abstract methods:
    ```java
    public abstract String getVehicleCategory();
    public abstract BigDecimal calculateCategoryCharge(int rentalDays);
    ```
- **`com.drivego.model.User` (Abstract Base Class)**
  - Marked with `public abstract class User`.
  - Defines common identity state while leaving role resolution abstract:
    ```java
    public abstract String getRole();
    ```

---

## 3. Inheritance

**Definition:** Mechanism where a subclass acquires the properties and behaviors of a superclass, promoting code reuse and establishing an *is-a* relationship.

### Implementation in DriveGo:
DriveGo implements a two-tier inheritance model backed by JPA `SINGLE_TABLE` persistence:

```
                  ┌──────────────┐
                  │     User     │ (Abstract Superclass)
                  └──────┬───────┘
                         │
             ┌───────────┴───────────┐
             ▼                       ▼
      ┌──────────────┐        ┌─────────────┐
      │   Customer   │        │    Admin    │ (Child Classes)
      └──────────────┘        └─────────────┘

                  ┌──────────────┐
                  │   Vehicle    │ (Abstract Superclass)
                  └──────┬───────┘
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
 ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
 │     Car     │  │    Bike     │  │     SUV     │ (Child Classes)
 └─────────────┘  └─────────────┘  └─────────────┘
```

1. **User Hierarchy:**
   - **Superclass:** `com.drivego.model.User`
   - **Subclasses:** `com.drivego.model.Customer` and `com.drivego.model.Admin`
   - Both inherit common attributes (`fullName`, `email`, `phone`, `createdAt`, `authProvider`) from `User`.
   - `Admin` adds a specialized `department` attribute; `Customer` adds `totalBookings`.

2. **Vehicle Hierarchy:**
   - **Superclass:** `com.drivego.model.Vehicle`
   - **Subclasses:** `com.drivego.model.Car`, `com.drivego.model.Bike`, and `com.drivego.model.SUV`
   - Inherits common properties (`brand`, `model`, `pricePerDay`, `securityDeposit`, `seats`, `fuelType`, `transmission`).
   - `Car` adds `numberOfDoors`.
   - `Bike` adds `bikeType` (Cruiser, Scooter, Sport).
   - `SUV` adds `fourWheelDrive` (4WD flag).

---

## 4. Polymorphism

**Definition:** The ability of an entity to take on multiple forms. DriveGo demonstrates both compile-time (overloading) and runtime (overriding & interface references) polymorphism.

### A. Runtime Polymorphism (Dynamic Method Dispatch)
- **Subclass Method Overriding:**
  - `Car.calculateCategoryCharge(int days)` returns `BigDecimal.ZERO`.
  - `Bike.calculateCategoryCharge(int days)` adds a mandatory ₹200/day helmet and insurance charge:
    $$\text{charge} = 200 \times \text{days}$$
  - `SUV.calculateCategoryCharge(int days)` adds a 10% premium surcharge on top of the base rental:
    $$\text{charge} = (\text{pricePerDay} \times \text{days}) \times 0.10$$
  - At runtime, `PricingServiceImpl` simply invokes:
    ```java
    BigDecimal categoryCharge = vehicle.calculateCategoryCharge(days);
    ```
    The JVM dynamically invokes the overridden method corresponding to whether `vehicle` is a `Car`, `Bike`, or `SUV`.

- **Interface Reference Polymorphism:**
  - Interface `com.drivego.payment.PaymentMethod` is implemented by:
    - `UPIPayment`
    - `CardPayment`
    - `CashPayment`
  - In `com.drivego.service.impl.BookingServiceImpl`:
    ```java
    PaymentMethod pm = paymentMethods.getOrDefault(dto.getPaymentMethod(), cashPayment);
    PaymentResult result = pm.pay(totalAmount); // Polymorphic call
    ```
  - The booking service depends solely on the `PaymentMethod` abstraction, not concrete payment providers.

### B. Compile-time Polymorphism (Method Overloading)
- **`com.drivego.service.PricingService` / `PricingServiceImpl`** provides three overloaded signatures for `calculatePrice`:
  1. `calculatePrice(Vehicle vehicle, LocalDate pickupDate, LocalDate returnDate)` — computes duration and delegates to (2).
  2. `calculatePrice(Vehicle vehicle, int days)` — standard calculation with zero discount.
  3. `calculatePrice(Vehicle vehicle, int days, BigDecimal discountAmount)` — full calculation supporting promotional discounts.

---

## 5. Interfaces

**Definition:** An abstract type that specifies a contract of method signatures without providing concrete implementation, enabling the Strategy Design Pattern and decoupling components.

### Implementation in DriveGo:
1. **`com.drivego.payment.PaymentMethod`**
   - Defines `pay(BigDecimal amount)` and `getMethodName()`.
   - Allows seamless addition of new payment channels (e.g. NetBanking, Crypto) without modifying the booking engine (Open-Closed Principle).
2. **`com.drivego.security.OtpSender`**
   - Defines `sendOtp(String phone, String otp)` and `getMode()`.
   - `ConsoleOtpSender` is active by default (logs OTP to server console with no cost or configuration).
   - `SmsOtpSender` acts as an easily activatable stub for production SMS gateways (Twilio, AWS SNS, MSG91).
3. **Service Contracts:**
   - `BookingService`, `VehicleService`, `UserService`, `PricingService`, and `OtpService` are all declared as interfaces, separating controller concerns from business logic implementations in `service.impl.*`.

---

## 6. Super and This Keywords

- **`super(...)`**:
  - Used in constructors of `Car`, `Bike`, `SUV`, `Customer`, and `Admin` to pass common parameters to parent constructors.
  - Example in `Car.java`:
    ```java
    public Car(String brand, String model, String registrationNumber,
               BigDecimal pricePerDay, BigDecimal securityDeposit, int numberOfDoors) {
        super(brand, model, registrationNumber, pricePerDay, securityDeposit);
        this.numberOfDoors = numberOfDoors;
    }
    ```
- **`this`**:
  - Disambiguates instance variables from constructor and setter parameters.
  - Used in chaining methods and referencing current object instances.

---

## 7. Static and Final Keywords

- **`static final` Constants:**
  - `Vehicle.TAX_RATE = new BigDecimal("0.05")` — 5% GST across all rentals.
  - `Vehicle.MAX_RENTAL_DAYS = 30` — system-wide rental duration cap.
- **`static` Utilities:**
  - `com.drivego.util.BookingCodeGenerator`
    - Contains static methods `generate()`, `generateInvoiceNumber()`, and `generateOtp()`.
    - Leverages thread-safe `SecureRandom` and `AtomicLong` counters without requiring object instantiation.
  - `com.drivego.util.PriceFormatter`
    - Provides static Indian rupee formatting utilities (`₹3,200`).
- **Static Factory Methods:**
  - `PaymentResult.success(txnRef, amount)` and `PaymentResult.failed(reason)`.

---

## 8. Exception Handling

**Definition:** Structured error-handling mechanism using custom domain exceptions and a centralized handler to ensure that unexpected errors never crash the application or expose internal stack traces to users.

### Hierarchy & Custom Exceptions (`com.drivego.exception.*`):
- `VehicleNotAvailableException` (thrown when vehicle is booked or inactive)
- `InvalidRentalDateException` (thrown when dates are in past, return $\le$ pickup, or duration $> 30$ days)
- `InvalidBookingException` (thrown on invalid state transitions or cross-user cancellation attempts)
- `InvalidPaymentException` (thrown when simulated payment processing fails)

### Centralized Controller Advice:
- **`com.drivego.exception.GlobalExceptionHandler`**
  - Annotated with `@ControllerAdvice`.
  - Intercepts all custom exceptions and renders user-friendly messages via `templates/error/friendly.html`.
  - Ensures WCAG AA compliance with clear instructions on how the customer can resolve the error (e.g. choose different dates).

---

## 9. Java Collections Framework

- **`java.util.List` & `java.util.ArrayList`:**
  - Used for storing query results, search filters, and active bookings.
- **`java.util.LinkedHashMap`:**
  - Used in `com.drivego.dto.PriceBreakdownDTO.getBreakdownMap()` to preserve the exact insertion order of line-item charges when rendering the invoice table.
- **`java.util.Map` & `java.util.HashMap`:**
  - Used in `BookingServiceImpl` to map payment method labels (`"UPI"`, `"CARD"`, `"CASH"`) to their respective `PaymentMethod` bean instances.
- **`java.util.Optional`:**
  - Returned by repositories (`userRepository.findByEmail(...)`, `bookingRepository.findByBookingCode(...)`) to safely handle nullable values and avoid `NullPointerException`.
- **Java Streams API:**
  - Used for filtering bookings by status, counting active reservations, and sorting vehicle listings by price (`Comparator`).

---

## Summary Matrix for Practical Viva

| OOP Topic | Primary Class | What to Explain to the Examiner |
|---|---|---|
| **Encapsulation** | `Vehicle.java`, `User.java` | "Private fields with business validation inside setters preventing negative prices." |
| **Abstraction** | `Vehicle.java` (abstract) | "Abstract method `calculateCategoryCharge()` forces Car/Bike/SUV to implement individual pricing." |
| **Inheritance** | `Car extends Vehicle`, `Customer extends User` | "JPA `SINGLE_TABLE` maps Java inheritance into normalized database tables." |
| **Polymorphism** | `PaymentMethod` $\to$ `UPIPayment`/`CardPayment` | "The service relies on the interface reference, choosing the implementation at runtime." |
| **Method Overloading** | `PricingService.calculatePrice(...)` | "3 overloads accepting dates, days, or days + promotional discounts." |
| **Method Overriding** | `Bike.calculateCategoryCharge(days)` | "Bike overrides superclass method to add ₹200/day helmet insurance." |
| **Exception Handling**| `GlobalExceptionHandler.java` | "`@ControllerAdvice` captures domain exceptions to show friendly error pages instead of stack traces." |
| **Collections** | `PriceBreakdownDTO.java` | "`LinkedHashMap` guarantees line-item display order for invoices." |
| **Design Patterns** | `PaymentMethod`, `PaymentResult` | "Strategy Pattern for payment methods and Static Factory Method for results." |

