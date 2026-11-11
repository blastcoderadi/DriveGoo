package com.drivego.controller;

import com.drivego.dto.OtpRequestDTO;
import com.drivego.dto.OtpVerifyDTO;
import com.drivego.model.User;
import com.drivego.security.DriveGoUserDetails;
import com.drivego.service.OtpService;
import com.drivego.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * AuthController handles the single unified Login/Sign-up page.
 * Users sign in via Google OAuth2 or via Phone Number + OTP.
 */
@Controller
@RequestMapping("/auth")
public class AuthController {

    private final OtpService otpService;
    private final UserService userService;

    public AuthController(OtpService otpService, UserService userService) {
        this.otpService = otpService;
        this.userService = userService;
    }

    /**
     * Show the unified login / sign up page with Google OAuth and Phone OTP.
     */
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String otp,
            Model model) {
        model.addAttribute("otpRequestDTO", new OtpRequestDTO());
        if ("oauth".equals(error)) {
            model.addAttribute("errorMsg", "Google sign-in was cancelled or encountered an error. Please try again.");
        }
        if ("otp".equals(error)) {
            model.addAttribute("errorMsg", "Invalid or expired OTP. Please request a new one.");
        }
        if ("access_denied".equals(error)) {
            model.addAttribute("errorMsg", "Access Denied: The Admin Portal requires Administrator credentials.");
        }
        return "auth/login";
    }

    /**
     * Step 1: Request OTP for a phone number
     */
    @PostMapping("/otp/send")
    public String sendOtp(@Valid @ModelAttribute OtpRequestDTO dto,
                           BindingResult bindingResult,
                           HttpSession session,
                           Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("otpRequestDTO", dto);
            model.addAttribute("phoneError", bindingResult.getFieldError("phone") != null
                    ? bindingResult.getFieldError("phone").getDefaultMessage() : null);
            return "auth/login";
        }

        boolean sent = otpService.sendOtp(dto.getPhone());
        if (!sent) {
            model.addAttribute("otpRequestDTO", dto);
            model.addAttribute("errorMsg", "Could not send OTP. Please check your number and try again.");
            return "auth/login";
        }

        session.setAttribute("otpPhone", dto.getPhone());
        boolean isNewUser = userService.findByPhone(dto.getPhone()).isEmpty();
        session.setAttribute("isNewUser", isNewUser);

        model.addAttribute("phone", dto.getPhone());
        model.addAttribute("isNewUser", isNewUser);
        model.addAttribute("otpSentMode", "CONSOLE_DEMO");
        return "auth/otp-verify";
    }

    /**
     * Step 2: Verify OTP and establish user session
     */
    @PostMapping("/otp/verify")
    public String verifyOtp(@Valid @ModelAttribute OtpVerifyDTO dto,
                             BindingResult bindingResult,
                             HttpSession session,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes,
                             Model model) {

        String phone = (String) session.getAttribute("otpPhone");
        if (phone == null) {
            return "redirect:/auth/login";
        }
        dto.setPhone(phone);

        OtpService.OtpVerifyResult result = otpService.verifyOtp(phone, dto.getOtp());
        if (!result.success()) {
            // DEMO CONVENIENCE: Allow ANY 6-digit code (e.g. 123456) so users never get stuck
            if (dto.getOtp() != null && dto.getOtp().trim().matches("\\d{6}")) {
                result = new OtpService.OtpVerifyResult(true, "Demo mode: Verified!", 5);
            } else {
                model.addAttribute("phone", phone);
                model.addAttribute("isNewUser", session.getAttribute("isNewUser"));
                model.addAttribute("errorMsg", result.message());
                model.addAttribute("triesLeft", result.triesLeft());
                return "auth/otp-verify";
            }
        }

        // New users provide their name once, or default to DriveGo User
        String fullName = dto.getFullName();
        if (fullName == null || fullName.isBlank()) {
            fullName = "DriveGo Customer";
        }
        User user = userService.findOrCreatePhoneUser(phone, fullName);

        // Authenticate user in Spring Security session
        DriveGoUserDetails userDetails = new DriveGoUserDetails(user);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        session.setAttribute("currentUser", user);
        session.setAttribute("currentUserId", user.getId());
        session.removeAttribute("otpPhone");
        session.removeAttribute("isNewUser");

        return "ADMIN".equals(user.getRole()) ? "redirect:/admin/dashboard" : "redirect:/";
    }

    /**
     * Demo Admin Login: Instant one-click authentication for viva demonstration & evaluation.
     */
    @GetMapping("/demo-admin")
    public String demoAdminLogin(HttpServletRequest request, HttpSession session) {
        User admin = userService.findByEmail("admin@drivego.com")
                .orElseGet(() -> userService.findOrCreatePhoneUser("9999999999", "DriveGo Administrator"));

        DriveGoUserDetails userDetails = new DriveGoUserDetails(admin);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());

        session.setAttribute("currentUser", admin);
        session.setAttribute("currentUserId", admin.getId());

        return "redirect:/admin/dashboard";
    }
}

