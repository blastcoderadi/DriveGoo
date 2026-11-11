package com.drivego.util;

import java.security.SecureRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * OOP Concept: STATIC keyword
 *
 * BookingCodeGenerator uses STATIC methods – no instance needed.
 * Uses SecureRandom for cryptographically secure randomness.
 *
 * Format: DG-XXXXXXXX  (DG + 8 uppercase alphanumeric characters)
 * Example: DG-A3F7K9Q2
 */
public final class BookingCodeGenerator {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final AtomicLong INVOICE_SEQ = new AtomicLong(1000);

    // Private constructor – utility class, not instantiable
    private BookingCodeGenerator() {}

    /**
     * Generate a booking code: DG-XXXXXXXX
     */
    public static String generate() {
        StringBuilder sb = new StringBuilder("DG-");
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Generate a short code for transaction references.
     */
    public static String generateShort() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    /**
     * Generate an invoice number: INV-YYYY-NNNNNN
     */
    public static String generateInvoiceNumber() {
        int year = java.time.Year.now().getValue();
        return String.format("INV-%d-%06d", year, INVOICE_SEQ.incrementAndGet());
    }

    /**
     * Generate a 6-digit numeric OTP string.
     */
    public static String generateOtp() {
        int otp = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }
}
