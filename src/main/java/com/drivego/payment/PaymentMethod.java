package com.drivego.payment;

import java.math.BigDecimal;

/**
 * OOP Concept: INTERFACE + POLYMORPHISM
 *
 * PaymentMethod defines the contract for all payment implementations.
 * UPIPayment, CardPayment and CashPayment all implement this.
 *
 * Usage (Polymorphism):
 *   PaymentMethod pm = new UPIPayment();
 *   PaymentResult result = pm.pay(amount); // Calls UPIPayment.pay()
 *
 * Adding a new payment method (e.g., NetBanking) only requires
 * implementing this interface – no other code changes needed.
 */
public interface PaymentMethod {

    /**
     * Process the payment for the given amount.
     * Returns a PaymentResult with success/failure details.
     */
    PaymentResult pay(BigDecimal amount);

    /**
     * Returns the display name of this payment method.
     * Example: "UPI", "Credit/Debit Card", "Cash on Pickup"
     */
    String getMethodName();
}
