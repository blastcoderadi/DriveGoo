package com.drivego.service;

import com.drivego.model.User;
import java.util.List;
import java.util.Optional;

public interface UserService {
    User findOrCreateGoogleUser(String email, String fullName);
    User findOrCreatePhoneUser(String phone, String fullName);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findById(Long id);
    User update(Long id, String fullName, String phone);
    List<User> findAllCustomers();
    List<User> searchCustomers(String query);
    long countCustomers();
    boolean isAdmin(String email);
}
