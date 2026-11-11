package com.drivego.controller;

import com.drivego.dto.BookingRequestDTO;
import com.drivego.dto.PriceBreakdownDTO;
import com.drivego.exception.InvalidBookingException;
import com.drivego.model.Booking;
import com.drivego.model.User;
import com.drivego.repository.InvoiceRepository;
import com.drivego.security.DriveGoUserDetails;
import com.drivego.service.BookingService;
import com.drivego.service.PricingService;
import com.drivego.service.UserService;
import com.drivego.service.VehicleService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/booking")
public class BookingController {

    private final BookingService bookingService;
    private final VehicleService vehicleService;
    private final PricingService pricingService;
    private final UserService userService;
    private final InvoiceRepository invoiceRepository;

    public BookingController(BookingService bookingService, VehicleService vehicleService,
                              PricingService pricingService, UserService userService,
                              InvoiceRepository invoiceRepository) {
        this.bookingService = bookingService;
        this.vehicleService = vehicleService;
        this.pricingService = pricingService;
        this.userService = userService;
        this.invoiceRepository = invoiceRepository;
    }

    /** Step 1: Select Dates and Location */
    @GetMapping("/new")
    public String showForm(@RequestParam Long vehicleId, Model model) {
        var vehicle = vehicleService.findById(vehicleId)
                .orElseThrow(() -> new InvalidBookingException("Vehicle not found."));
        BookingRequestDTO dto = new BookingRequestDTO();
        dto.setVehicleId(vehicleId);
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("bookingDTO", dto);
        return "booking/step1-dates";
    }

    /** Step 2: Review Breakdown and Select Payment Method */
    @PostMapping("/preview")
    public String previewPrice(@Valid @ModelAttribute("bookingDTO") BookingRequestDTO dto,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            var vehicle = vehicleService.findById(dto.getVehicleId()).orElse(null);
            model.addAttribute("vehicle", vehicle);
            return "booking/step1-dates";
        }

        var vehicle = vehicleService.findById(dto.getVehicleId())
                .orElseThrow(() -> new InvalidBookingException("Vehicle not found."));

        if (!bookingService.isAvailable(vehicle.getId(), dto.getPickupDate(), dto.getReturnDate())) {
            model.addAttribute("vehicle", vehicle);
            model.addAttribute("bookingDTO", dto);
            model.addAttribute("availabilityError",
                    vehicle.getDisplayName() + " is already booked for the selected dates. Please choose different dates.");
            return "booking/step1-dates";
        }

        PriceBreakdownDTO price = pricingService.calculatePrice(vehicle, dto.getPickupDate(), dto.getReturnDate());
        model.addAttribute("vehicle", vehicle);
        model.addAttribute("bookingDTO", dto);
        model.addAttribute("price", price);
        return "booking/step2-review";
    }

    /** Step 3: Confirm and execute booking */
    @PostMapping("/confirm")
    public String confirmBooking(@ModelAttribute("bookingDTO") BookingRequestDTO dto,
                                  BindingResult result,
                                  @AuthenticationPrincipal Object principal,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (dto.getVehicleId() == null) {
            return "redirect:/vehicles";
        }

        User user = resolveUser(principal, session);
        if (user == null) {
            // Demo fallback customer so bookings work seamlessly even if guest hadn't logged in first
            user = userService.findOrCreatePhoneUser("9876543210", "DriveGo Customer");
            session.setAttribute("currentUser", user);
            session.setAttribute("currentUserId", user.getId());
        }

        try {
            if (dto.getPaymentMethod() == null || dto.getPaymentMethod().isBlank()) {
                dto.setPaymentMethod("UPI");
            }
            Booking booking = bookingService.createBooking(dto, user);
            return "redirect:/booking/confirmation/" + booking.getBookingCode();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/booking/new?vehicleId=" + dto.getVehicleId();
        }
    }

    /** Step 4: Show Confirmation and Invoice (supports both GET and POST) */
    @RequestMapping(value = "/confirmation/{code}", method = {RequestMethod.GET, RequestMethod.POST})
    public String confirmationPage(@PathVariable String code,
                                    @AuthenticationPrincipal Object principal,
                                    HttpSession session, Model model) {
        Booking booking = bookingService.findByCode(code)
                .orElseThrow(() -> new InvalidBookingException("Booking reference not found: " + code));

        User user = resolveUser(principal, session);
        if (user == null) {
            user = booking.getUser();
        }

        var invoice = invoiceRepository.findByBookingId(booking.getId()).orElse(null);
        model.addAttribute("booking", booking);
        model.addAttribute("invoice", invoice);
        model.addAttribute("user", user);
        return "booking/step3-confirmation";
    }

    /** Customer cancellation */
    @PostMapping("/cancel/{id}")
    public String cancel(@PathVariable Long id,
                          @AuthenticationPrincipal Object principal,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal, session);
        if (user == null) return "redirect:/auth/login";

        try {
            bookingService.cancelBooking(id, user);
            redirectAttributes.addFlashAttribute("successMsg", "Booking cancelled successfully. Refund initiated.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/customer/bookings";
    }

    private User resolveUser(Object principal, HttpSession session) {
        if (principal instanceof DriveGoUserDetails dud) {
            return dud.getUser();
        } else if (principal instanceof OAuth2User oauth2User) {
            String email = oauth2User.getAttribute("email");
            if (email != null) {
                return userService.findByEmail(email).orElse(null);
            }
        }
        Long userId = (Long) session.getAttribute("currentUserId");
        if (userId != null) {
            return userService.findById(userId).orElse(null);
        }
        return null;
    }
}

