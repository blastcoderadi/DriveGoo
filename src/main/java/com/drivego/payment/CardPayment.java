package com.drivego.payment;

import com.drivego.util.BookingCodeGenerator;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * OOP Concept: INTERFACE IMPLEMENTATION + POLYMORPHISM
 * CardPayment simulates a debit/credit card payment.
 */
@Component
public class CardPayment implements PaymentMethod {

    @Override
    public PaymentResult pay(BigDecimal amount) {
        System.out.println("[CARD PAYMENT SIMULATED] Amount: ₹" + amount);
        String txnRef = "CARD-" + BookingCodeGenerator.generateShort();
        return PaymentResult.success(txnRef, amount);
    }

    @Override
    public String getMethodName() {
        return "Credit/Debit Card";
    }
}
