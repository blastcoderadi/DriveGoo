package com.drivego.exception;

import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * OOP Concept: EXCEPTION HANDLING
 * @ControllerAdvice catches all custom exceptions across all controllers.
 * Never shows a raw stack trace to users.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VehicleNotAvailableException.class)
    public String handleUnavailable(VehicleNotAvailableException ex, Model model) {
        model.addAttribute("title", "Vehicle Not Available");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("icon", "bi-car-front");
        return "error/friendly";
    }

    @ExceptionHandler(InvalidRentalDateException.class)
    public String handleInvalidDate(InvalidRentalDateException ex, Model model) {
        model.addAttribute("title", "Invalid Dates");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("icon", "bi-calendar-x");
        return "error/friendly";
    }

    @ExceptionHandler(InvalidBookingException.class)
    public String handleInvalidBooking(InvalidBookingException ex, Model model) {
        model.addAttribute("title", "Booking Error");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("icon", "bi-exclamation-circle");
        return "error/friendly";
    }

    @ExceptionHandler(InvalidPaymentException.class)
    public String handlePayment(InvalidPaymentException ex, Model model) {
        model.addAttribute("title", "Payment Error");
        model.addAttribute("message", ex.getMessage());
        model.addAttribute("icon", "bi-credit-card-2-back");
        return "error/friendly";
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(Model model) {
        model.addAttribute("title", "Page Not Found");
        model.addAttribute("message", "The page you're looking for doesn't exist.");
        model.addAttribute("icon", "bi-search");
        return "error/friendly";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class).error("Unhandled exception: ", ex);
        model.addAttribute("title", "Something Went Wrong");
        model.addAttribute("message", ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred. Please try again.");
        model.addAttribute("icon", "bi-tools");
        return "error/friendly";
    }
}

