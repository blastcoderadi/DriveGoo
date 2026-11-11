package com.drivego.service;

public interface OtpService {
    /** Send a fresh OTP to the phone number. Returns true on success. */
    boolean sendOtp(String phone);

    /**
     * Verify the OTP. Returns the result.
     * OtpVerifyResult carries success flag, error message, triesLeft.
     */
    OtpVerifyResult verifyOtp(String phone, String otp);

    record OtpVerifyResult(boolean success, String message, int triesLeft) {}
}
