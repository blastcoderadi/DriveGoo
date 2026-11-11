package com.drivego.repository;

import com.drivego.model.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<OtpCode, Long> {

    // Get latest unused OTP for a phone number
    @Query("SELECT o FROM OtpCode o WHERE o.phone = :phone AND o.verified = false " +
           "ORDER BY o.createdAt DESC LIMIT 1")
    Optional<OtpCode> findLatestByPhone(@Param("phone") String phone);

    // Delete old OTPs for a phone (cleanup before issuing new one)
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.phone = :phone")
    void deleteByPhone(@Param("phone") String phone);
}

