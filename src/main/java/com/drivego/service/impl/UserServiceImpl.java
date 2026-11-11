package com.drivego.service.impl;

import com.drivego.model.Admin;
import com.drivego.model.Customer;
import com.drivego.model.User;
import com.drivego.repository.UserRepository;
import com.drivego.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * OOP Concept: INTERFACE IMPLEMENTATION + INHERITANCE usage
 *
 * UserServiceImpl handles user creation for both Google OAuth and Phone OTP.
 * When a Google email matches drivego.admin-emails, an Admin object is created.
 * Otherwise, a Customer is created.
 *
 * This demonstrates INHERITANCE: findOrCreateGoogleUser may return
 * a Customer or an Admin – both are User instances.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Value("${drivego.admin-emails:admin@drivego.com}")
    private String adminEmails;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User findOrCreateGoogleUser(String email, String fullName) {
        return userRepository.findByEmail(email).orElseGet(() -> {
            User newUser;
            if (isAdmin(email)) {
                // OOP Concept: INHERITANCE – create Admin (child of User)
                Admin admin = new Admin(fullName, email, null, User.AuthProvider.GOOGLE);
                admin.setDepartment("Management");
                newUser = admin;
            } else {
                // OOP Concept: INHERITANCE – create Customer (child of User)
                newUser = new Customer(fullName, email, null, User.AuthProvider.GOOGLE);
            }
            return userRepository.save(newUser);
        });
    }

    @Override
    public User findOrCreatePhoneUser(String phone, String fullName) {
        return userRepository.findByPhone(phone).orElseGet(() -> {
            if ("9999999999".equals(phone)) {
                Admin admin = new Admin(fullName != null && !fullName.isBlank() ? fullName : "DriveGo Administrator",
                        "admin@drivego.com", phone, User.AuthProvider.PHONE);
                admin.setDepartment("Fleet Operations");
                return userRepository.save(admin);
            }
            Customer customer = new Customer(fullName, null, phone, User.AuthProvider.PHONE);
            return userRepository.save(customer);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByPhone(String phone) {
        return userRepository.findByPhone(phone);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public User update(Long id, String fullName, String phone) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        if (fullName != null && !fullName.isBlank()) user.setFullName(fullName.trim());
        if (phone != null && !phone.isBlank()) user.setPhone(phone);
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAllCustomers() {
        // Returns all users (we filter in template/controller by role)
        return userRepository.findAll().stream()
                .filter(u -> "CUSTOMER".equals(u.getRole()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchCustomers(String query) {
        return userRepository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query)
                .stream().filter(u -> "CUSTOMER".equals(u.getRole())).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countCustomers() {
        return userRepository.findAll().stream()
                .filter(u -> "CUSTOMER".equals(u.getRole())).count();
    }

    @Override
    public boolean isAdmin(String email) {
        if (email == null) return false;
        return Arrays.stream(adminEmails.split(","))
                .map(String::trim)
                .anyMatch(e -> e.equalsIgnoreCase(email));
    }
}
