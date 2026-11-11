package com.drivego.service.impl;

import com.drivego.model.OtpCode;
import com.drivego.repository.OtpRepository;
import com.drivego.security.OtpSender;
import com.drivego.service.OtpService;
import com.drivego.util.BookingCodeGenerator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

/**
 * OOP Concept: INTERFACE IMPLEMENTATION + EXCEPTION HANDLING + OtpSender POLYMORPHISM
 *
 * OtpServiceImpl manages OTP lifecycle:
 * generate → hash (BCrypt) → send (via OtpSender interface) → verify → expire/block
 */
@Service
@Transactional
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpSender otpSender; // OOP: POLYMORPHISM – Console or SMS

    @Value("${drivego.otp.expiry-minutes:5}")
    private int expiryMinutes;

    @Value("${drivego.otp.max-attempts:5}")
    private int maxAttempts;

    @Value("${drivego.otp.block-minutes:10}")
    private int blockMinutes;

    public OtpServiceImpl(OtpRepository otpRepository,
                          PasswordEncoder passwordEncoder,
                          OtpSender otpSender) {
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.otpSender = otpSender;
    }

    @Override
    public boolean sendOtp(String phone) {
        // Delete any old OTPs for this phone
        otpRepository.deleteByPhone(phone);

        String rawOtp = BookingCodeGenerator.generateOtp(); // 6-digit OTP
        String otpHash = passwordEncoder.encode(rawOtp);    // BCrypt hash

        OtpCode otpCode = new OtpCode(
                phone,
                otpHash,
                LocalDateTime.now().plusMinutes(expiryMinutes)
        );
        otpRepository.save(otpCode);

        // OTP is sent via OtpSender (ConsoleOtpSender by default)
        return otpSender.sendOtp(phone, rawOtp);
    }

    @Override
    @Transactional
    public OtpVerifyResult verifyOtp(String phone, String otp) {
        OtpCode otpCode = otpRepository.findLatestByPhone(phone).orElse(null);

        if (otpCode == null) {
            return new OtpVerifyResult(false, "No OTP sent to this number. Please request one.", 0);
        }

        if (otpCode.isBlocked()) {
            return new OtpVerifyResult(false,
                    "Too many wrong attempts. Try again after " + blockMinutes + " minutes.", 0);
        }

        if (otpCode.isExpired()) {
            return new OtpVerifyResult(false, "OTP has expired. Please request a new one.", 0);
        }

        // Verify using BCrypt – constant-time comparison
        boolean matches = passwordEncoder.matches(otp, otpCode.getOtpHash());

        if (!matches) {
            otpCode.incrementAttempts();
            if (otpCode.getAttempts() >= maxAttempts) {
                otpCode.setBlockedUntil(LocalDateTime.now().plusMinutes(blockMinutes));
                otpRepository.save(otpCode);
                return new OtpVerifyResult(false,
                        "Too many wrong attempts. Blocked for " + blockMinutes + " minutes.", 0);
            }
            otpRepository.save(otpCode);
            int triesLeft = maxAttempts - otpCode.getAttempts();
            return new OtpVerifyResult(false,
                    "That code is not correct. " + triesLeft + " " +
                    (triesLeft == 1 ? "try" : "tries") + " left.", triesLeft);
        }

        // Mark as verified so it cannot be reused
        otpCode.setVerified(true);
        otpRepository.save(otpCode);
        return new OtpVerifyResult(true, "Verified successfully.", maxAttempts);
    }
}
