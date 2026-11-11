# DriveGo – Vehicle Rental Management System

DriveGo is a full-featured, university-level Java Vehicle Rental Management System built with **Spring Boot 3, Thymeleaf, Spring Data JPA, Spring Security, and MySQL/H2**.

---

## 1. Quick Start (Run in 1 Step)

The project includes an in-memory **H2 `dev` profile** so it runs immediately on any computer with Java 17+ installed, with **zero external database setup needed**.

```bash
cd c:\antigravity\vechical-rental-system\drivego

# Run using the dev profile (H2 in-memory DB + auto seed)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Once started, open your browser to:
👉 **http://localhost:8080**

H2 Database Console:
👉 **http://localhost:8080/h2-console** (JDBC URL: `jdbc:h2:mem:drivegodb`, User: `sa`, Password: empty)

---

## 2. Running with MySQL 8 (Production Profile)

1. Create the MySQL database:
```sql
CREATE DATABASE IF NOT EXISTS drivego CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Set environment variables (optional, defaults to `root` with empty password):
```bash
set DB_USER=root
set DB_PASSWORD=your_password
```

3. Run the application:
```bash
mvn spring-boot:run
```

---

## 3. Demo Credentials & Authentication

DriveGo provides a **unified "Continue" screen** where users never have to choose between "login" or "sign up".

### Option A: Phone Number + OTP (Zero configuration required)
- Enter any valid 10-digit Indian mobile number (e.g. `9876543210`).
- Click **"Send OTP"**.
- DriveGo uses `ConsoleOtpSender` in demo mode: the **6-digit OTP will be printed directly in your terminal console**.
- Enter the 6-digit OTP (e.g. `123456`) and click **"Verify & Continue"**.
- First-time users are prompted for their name once and immediately redirected to the home page.

### Option B: Demo Admin One-Click Login
- Go directly to: `http://localhost:8080/auth/demo-admin`
- Or on `/auth/login`, click **"One-Click Demo Admin Login"**.
- Or sign in with Admin Mobile: **`9999999999`** (OTP: `123456`).
- You will be immediately authenticated with `ROLE_ADMIN` and redirected to the **Admin Dashboard** (`/admin/dashboard`).

### Option C: Google OAuth2 Login
To enable real Google sign-in:
1. Create OAuth2 credentials at [Google Cloud Console](https://console.cloud.google.com/apis/credentials).
2. Set Authorized redirect URI to:
   `http://localhost:8080/login/oauth2/code/google`
3. Set your environment variables:
   ```bash
   set GOOGLE_CLIENT_ID=your-client-id.apps.googleusercontent.com
   set GOOGLE_CLIENT_SECRET=your-client-secret
   ```
4. Any Google account matching `drivego.admin-emails` in `application.properties` (default: `admin@drivego.com`) will automatically receive the **ADMIN** role. All other users receive the **CUSTOMER** role.

---

## 4. Git & GitHub Setup

To push this project to GitHub, open your terminal and run:

```bash
cd c:\antigravity\vechical-rental-system\drivego

# Initialize Git
git init

# Add all project source files (.gitignore will exclude target/ build files)
git add .

# Create initial commit
git commit -m "Initial commit: DriveGo - Vehicle Rental Management System"

# Rename branch to main
git branch -M main

# Link to your remote GitHub repository
git remote add origin https://github.com/<your-username>/<your-repo-name>.git

# Push to GitHub
git push -u origin main
```

---

## 4. Key Features

### Customer Features
- **Explore Vehicles:** Filter by Car, Bike, or SUV; search by brand/model; filter by price range; sort by price.
- **Real-Time Availability Check:** Prevents double-booking overlapping dates with existing bookings using database locking (`@Lock(PESSIMISTIC_WRITE)`).
- **Transparent Server-Side Pricing:** Full itemized breakdown before checkout:
  $$\text{Total} = (\text{Daily Rate} \times \text{Days}) + \text{Category Surcharge} + 5\% \text{ GST} + \text{Security Deposit}$$
- **Simulated Payment:** Interactive simulation supporting UPI, Debit/Credit Card, and Cash on Pickup via the `PaymentMethod` interface.
- **Booking Lifecycle:** `PENDING` $\to$ `CONFIRMED` $\to$ `ACTIVE` $\to$ `COMPLETED` or `CANCELLED`.
- **Instant Tax Invoice:** Printable receipt with unique invoice number (`INV-2026-XXXXXX`) and booking code (`DG-XXXXXXXX`).
- **Customer Dashboard & Profile:** Manage reservations, view upcoming trips, and cancel eligible future bookings.

### Admin Features (`/admin/**`)
- **Fleet Operations Dashboard:** Aggregated revenue (`COALESCE(SUM(b.totalAmount), 0)`), fleet counts, booking status breakdown.
- **Manage Vehicles:** Add, edit, toggle availability, or delete vehicles (deletion blocked if active bookings exist).
- **Manage Bookings:** Approve pending reservations, reject requests with reason, or mark completed with automatic late return fee calculation.
- **Customer Directory:** View all registered customers and their authentication provider.
- **Payment Records:** Audit log of all transactions and total collected amount.

---

## 5. Technology Stack
- **Language:** Java 17
- **Framework:** Spring Boot 3.3.5
- **ORM & Persistence:** Spring Data JPA, Hibernate (Single-Table Inheritance)
- **Security:** Spring Security 6 (OAuth2 Client + Phone OTP Session Auth)
- **Templates:** Thymeleaf + Thymeleaf Extras Spring Security 6
- **Database:** MySQL 8 (Production) / H2 In-Memory (Development)
- **Styling:** Bootstrap 5, Bootstrap Icons, Google Fonts (Inter)
- **Testing:** JUnit 5, Spring Boot Test, MockMvc

