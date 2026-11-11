package com.drivego.model;

import jakarta.persistence.*;

/**
 * OOP Concept: INHERITANCE (Child of User)
 * Customer extends User. Uses SINGLE_TABLE strategy.
 * The discriminator value "CUSTOMER" is stored in the 'role' column.
 *
 * OOP Concept: SUPER keyword – calls parent constructor.
 */
@Entity
@DiscriminatorValue("CUSTOMER")
public class Customer extends User {

    // Customer-specific: how many total bookings
    @Column(name = "total_bookings")
    private int totalBookings = 0;

    // ===== Constructors =====
    public Customer() {
        super(); // OOP Concept: SUPER keyword
    }

    public Customer(String fullName, String email, String phone, AuthProvider authProvider) {
        super(fullName, email, phone, authProvider); // OOP Concept: SUPER keyword
    }

    @Override
    public String getRole() {
        return "CUSTOMER"; // OOP Concept: METHOD OVERRIDING
    }

    public int getTotalBookings() { return totalBookings; }
    public void setTotalBookings(int totalBookings) { this.totalBookings = totalBookings; }
    public void incrementBookings() { this.totalBookings++; }
}
