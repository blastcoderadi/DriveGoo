package com.drivego.payment;

import com.drivego.util.BookingCodeGenerator;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * OOP Concept: INTERFACE IMPLEMENTATION + POLYMORPHISM
 *
 * UPIPayment implements PaymentMethod.
 * Simulates a UPI payment (no real gateway).
 *
 * @Component makes it a Spring-managed bean so it can be injected.
 */
@Component
public class UPIPayment implements PaymentMethod {

    @Override
    public PaymentResult pay(BigDecimal amount) {
        // Simulated – in real world: call UPI payment gateway API here
        System.out.println("[UPI PAYMENT SIMULATED] Amount: ₹" + amount);
        String txnRef = "UPI-" + BookingCodeGenerator.generateShort();
        return PaymentResult.success(txnRef, amount);
    }

    @Override
    public String getMethodName() {
        return "UPI";
    }
}
