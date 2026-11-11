package com.drivego.payment;

import com.drivego.util.BookingCodeGenerator;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;

/**
 * OOP Concept: INTERFACE IMPLEMENTATION + POLYMORPHISM
 * CashPayment – customer pays cash at pickup location.
 */
@Component
public class CashPayment implements PaymentMethod {

    @Override
    public PaymentResult pay(BigDecimal amount) {
        System.out.println("[CASH PAYMENT] Amount: ₹" + amount + " – to be collected at pickup.");
        String txnRef = "CASH-" + BookingCodeGenerator.generateShort();
        return PaymentResult.success(txnRef, amount);
    }

    @Override
    public String getMethodName() {
        return "Cash on Pickup";
    }
}
