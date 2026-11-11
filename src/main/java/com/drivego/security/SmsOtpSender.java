package com.drivego.security;

/**
 * OOP Concept: INTERFACE STUB (Polymorphism / Open-Closed Principle)
 *
 * SmsOtpSender is a stub for a real SMS gateway (e.g., Twilio, MSG91).
 * It is NOT a @Component by default – to activate it:
 *   1. Add the SMS provider's SDK to pom.xml
 *   2. Add @Component annotation here
 *   3. Set API key in application.properties
 *
 * Everything else in the codebase continues to work unchanged because
 * it only depends on the OtpSender interface.
 */
public class SmsOtpSender implements OtpSender {

    @Override
    public boolean sendOtp(String phone, String otp) {
        // TODO (future): Integrate real SMS provider here
        // Example: twilioClient.messages.create(phone, "Your DriveGo OTP: " + otp)
        throw new UnsupportedOperationException(
                "SmsOtpSender is a stub. Configure a real SMS provider.");
    }

    @Override
    public String getMode() {
        return "SMS";
    }
}
