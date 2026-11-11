package com.drivego.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * OOP Concept: INTERFACE IMPLEMENTATION
 *
 * ConsoleOtpSender is the DEFAULT OTP sender.
 * It prints the OTP to the server console/log.
 * No SMS service or payment required – perfect for demo and development.
 *
 * @ConditionalOnMissingBean: If SmsOtpSender is configured, this is not used.
 * This shows how interfaces make implementations swappable.
 */
@Component
@ConditionalOnMissingBean(SmsOtpSender.class)
public class ConsoleOtpSender implements OtpSender {

    private static final Logger log = LoggerFactory.getLogger(ConsoleOtpSender.class);

    @Override
    public boolean sendOtp(String phone, String otp) {
        log.info("============================================");
        log.info("  DRIVEGO OTP DEMO MODE");
        log.info("  Phone: {}  OTP: {}", phone, otp);
        log.info("  (In production, this would be sent via SMS)");
        log.info("============================================");
        return true; // Always succeeds in console mode
    }

    @Override
    public String getMode() {
        return "CONSOLE_DEMO";
    }
}
