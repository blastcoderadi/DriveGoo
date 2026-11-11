package com.drivego.payment;

import java.math.BigDecimal;

/**
 * OOP Concept: ENCAPSULATION + STATIC FACTORY METHODS
 *
 * PaymentResult holds the outcome of a payment attempt.
 * Uses static factory methods (success/failed) instead of constructors directly
 * – a common design pattern for self-documenting code.
 */
public class PaymentResult {

    private final boolean successful;
    private final String transactionRef;
    private final String message;
    private final BigDecimal amount;

    // Private constructor – use static factory methods
    private PaymentResult(boolean successful, String transactionRef,
                          String message, BigDecimal amount) {
        this.successful = successful;
        this.transactionRef = transactionRef;
        this.message = message;
        this.amount = amount;
    }

    /**
     * OOP Concept: STATIC FACTORY METHOD
     * Creates a successful payment result.
     */
    public static PaymentResult success(String transactionRef, BigDecimal amount) {
        return new PaymentResult(true, transactionRef,
                "Payment of ₹" + amount + " processed successfully (simulated).", amount);
    }

    /**
     * OOP Concept: STATIC FACTORY METHOD
     * Creates a failed payment result.
     */
    public static PaymentResult failed(String reason) {
        return new PaymentResult(false, null, reason, BigDecimal.ZERO);
    }

    public boolean isSuccessful() { return successful; }
    public String getTransactionRef() { return transactionRef; }
    public String getMessage() { return message; }
    public BigDecimal getAmount() { return amount; }

    @Override
    public String toString() {
        return "PaymentResult{success=" + successful + ", ref=" + transactionRef +
               ", message=" + message + "}";
    }
}
