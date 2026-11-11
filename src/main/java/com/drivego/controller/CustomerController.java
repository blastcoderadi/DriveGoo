package com.drivego.controller;

import com.drivego.model.Booking;
import com.drivego.model.User;
import com.drivego.security.DriveGoUserDetails;
import com.drivego.service.BookingService;
import com.drivego.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;

@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final UserService userService;
    private final BookingService bookingService;

    public CustomerController(UserService userService, BookingService bookingService) {
        this.userService = userService;
        this.bookingService = bookingService;
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal Object principal,
                             HttpSession session, Model model) {
        User user = resolveUser(principal, session);
        if (user == null) return "redirect:/auth/login";

        List<Booking> bookings = bookingService.findByUser(user);
        long upcoming = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED
                          || b.getStatus() == Booking.BookingStatus.ACTIVE).count();
        long pending = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.PENDING).count();

        model.addAttribute("user", user);
        model.addAttribute("recentBookings", bookings.stream().limit(3).toList());
        model.addAttribute("totalBookings", bookings.size());
        model.addAttribute("upcomingBookings", upcoming);
        model.addAttribute("pendingBookings", pending);
        return "customer/dashboard";
    }

    @GetMapping("/bookings")
    public String bookings(@AuthenticationPrincipal Object principal,
                            HttpSession session, Model model) {
        User user = resolveUser(principal, session);
        if (user == null) return "redirect:/auth/login";
        model.addAttribute("bookings", bookingService.findByUser(user));
        model.addAttribute("user", user);
        return "customer/bookings";
    }

    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal Object principal,
                           HttpSession session, Model model) {
        User user = resolveUser(principal, session);
        if (user == null) return "redirect:/auth/login";
        model.addAttribute("user", user);
        return "customer/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@AuthenticationPrincipal Object principal,
                                 HttpSession session,
                                 @RequestParam String fullName,
                                 @RequestParam(required = false) String phone,
                                 RedirectAttributes redirectAttributes) {
        User user = resolveUser(principal, session);
        if (user == null) return "redirect:/auth/login";
        try {
            userService.update(user.getId(), fullName, phone);
            redirectAttributes.addFlashAttribute("successMsg", "Profile updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/customer/profile";
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
        Long id = (Long) session.getAttribute("currentUserId");
        return id != null ? userService.findById(id).orElse(null) : null;
    }
}

