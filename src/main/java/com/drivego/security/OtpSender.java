package com.drivego.security;

/**
 * OOP Concept: INTERFACE (Strategy pattern for OTP delivery)
 *
 * OtpSender defines the contract for sending OTPs.
 * ConsoleOtpSender is the default (prints to log – no paid service needed).
 * SmsOtpSender is the stub for a real SMS gateway (plug in later by config).
 */
public interface OtpSender {

    /**
     * Send the OTP to the given phone number.
     * Returns true if sending was successful.
     */
    boolean sendOtp(String phone, String otp);

    /**
     * Returns the mode name for display purposes.
     * ConsoleOtpSender returns "CONSOLE_DEMO".
     */
    String getMode();
}
