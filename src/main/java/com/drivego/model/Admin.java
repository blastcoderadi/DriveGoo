package com.drivego.model;

import jakarta.persistence.*;

/**
 * OOP Concept: INHERITANCE (Child of User)
 * Admin extends User. Discriminator value "ADMIN".
 * Admins have an extra 'department' field not present in Customer.
 *
 * OOP Concept: SUPER keyword.
 */
@Entity
@DiscriminatorValue("ADMIN")
public class Admin extends User {

    @Column(name = "department")
    private String department;

    public Admin() {
        super();
    }

    public Admin(String fullName, String email, String phone, AuthProvider authProvider) {
        super(fullName, email, phone, authProvider);
    }

    @Override
    public String getRole() {
        return "ADMIN"; // OOP Concept: METHOD OVERRIDING
    }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
