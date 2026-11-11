package com.drivego.controller;

import com.drivego.dto.VehicleFormDTO;
import com.drivego.model.Booking;
import com.drivego.repository.PaymentRepository;
import com.drivego.service.*;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.math.BigDecimal;
import java.util.Arrays;

/**
 * AdminController handles /admin/** endpoints.
 * Role-based access ensures only users with ROLE_ADMIN can access.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final VehicleService vehicleService;
    private final BookingService bookingService;
    private final UserService userService;
    private final PaymentRepository paymentRepository;

    public AdminController(VehicleService vehicleService, BookingService bookingService,
                            UserService userService, PaymentRepository paymentRepository) {
        this.vehicleService = vehicleService;
        this.bookingService = bookingService;
        this.userService = userService;
        this.paymentRepository = paymentRepository;
    }

    // ===== Dashboard =====
    @GetMapping({"", "/", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("totalVehicles", vehicleService.count());
        model.addAttribute("totalCustomers", userService.countCustomers());
        model.addAttribute("totalBookings", bookingService.findAll().size());
        model.addAttribute("pendingBookings", bookingService.countByStatus(Booking.BookingStatus.PENDING));
        model.addAttribute("confirmedBookings", bookingService.countByStatus(Booking.BookingStatus.CONFIRMED));
        model.addAttribute("completedBookings", bookingService.countByStatus(Booking.BookingStatus.COMPLETED));
        model.addAttribute("cancelledBookings", bookingService.countByStatus(Booking.BookingStatus.CANCELLED));
        BigDecimal totalRevenue = bookingService.getTotalRevenue();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        model.addAttribute("recentBookings", bookingService.findAll().stream().limit(5).toList());
        return "admin/dashboard";
    }

    // ===== Manage Vehicles =====
    @GetMapping("/vehicles")
    public String vehicles(Model model) {
        model.addAttribute("vehicles", vehicleService.findAll());
        return "admin/vehicles";
    }

    @GetMapping("/vehicles/new")
    public String newVehicleForm(Model model) {
        model.addAttribute("vehicleForm", new VehicleFormDTO());
        model.addAttribute("vehicleTypes", Arrays.asList("CAR", "BIKE", "SUV"));
        return "admin/vehicle-form";
    }

    @PostMapping("/vehicles/save")
    public String saveVehicle(@Valid @ModelAttribute("vehicleForm") VehicleFormDTO dto,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        if (result.hasErrors()) {
            model.addAttribute("vehicleTypes", Arrays.asList("CAR", "BIKE", "SUV"));
            return "admin/vehicle-form";
        }
        try {
            vehicleService.save(dto);
            redirectAttributes.addFlashAttribute("successMsg", "Vehicle added successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/vehicles";
    }

    @GetMapping("/vehicles/edit/{id}")
    public String editVehicleForm(@PathVariable Long id, Model model) {
        var vehicle = vehicleService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found: " + id));
        VehicleFormDTO dto = new VehicleFormDTO();
        dto.setBrand(vehicle.getBrand());
        dto.setModel(vehicle.getModel());
        dto.setVehicleType(vehicle.getVehicleCategory().toUpperCase());
        dto.setRegistrationNumber(vehicle.getRegistrationNumber());
        dto.setPricePerDay(vehicle.getPricePerDay());
        dto.setSecurityDeposit(vehicle.getSecurityDeposit());
        dto.setSeats(vehicle.getSeats());
        dto.setFuelType(vehicle.getFuelType());
        dto.setTransmission(vehicle.getTransmission());
        dto.setDescription(vehicle.getDescription());
        dto.setImageUrl(vehicle.getImageUrl());
        dto.setActive(vehicle.isActive());

        model.addAttribute("vehicleForm", dto);
        model.addAttribute("vehicleId", id);
        model.addAttribute("vehicleTypes", Arrays.asList("CAR", "BIKE", "SUV"));
        return "admin/vehicle-form";
    }

    @PostMapping("/vehicles/update/{id}")
    public String updateVehicle(@PathVariable Long id,
                                 @Valid @ModelAttribute("vehicleForm") VehicleFormDTO dto,
                                 BindingResult result,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("vehicleId", id);
            model.addAttribute("vehicleTypes", Arrays.asList("CAR", "BIKE", "SUV"));
            return "admin/vehicle-form";
        }
        try {
            vehicleService.update(id, dto);
            redirectAttributes.addFlashAttribute("successMsg", "Vehicle updated successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/vehicles";
    }

    @PostMapping("/vehicles/delete/{id}")
    public String deleteVehicle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            vehicleService.delete(id);
            redirectAttributes.addFlashAttribute("successMsg", "Vehicle deleted successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/vehicles";
    }

    @PostMapping("/vehicles/toggle/{id}")
    public String toggleVehicle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        vehicleService.toggleActive(id);
        redirectAttributes.addFlashAttribute("successMsg", "Vehicle status updated.");
        return "redirect:/admin/vehicles";
    }

    // ===== Manage Bookings =====
    @GetMapping("/bookings")
    public String bookings(@RequestParam(required = false) String status, Model model) {
        if (status != null && !status.isBlank()) {
            try {
                Booking.BookingStatus bs = Booking.BookingStatus.valueOf(status.toUpperCase());
                model.addAttribute("bookings", bookingService.findByStatus(bs));
                model.addAttribute("selectedStatus", status);
            } catch (IllegalArgumentException e) {
                model.addAttribute("bookings", bookingService.findAll());
            }
        } else {
            model.addAttribute("bookings", bookingService.findAll());
        }
        model.addAttribute("statuses", Booking.BookingStatus.values());
        return "admin/bookings";
    }

    @PostMapping("/bookings/confirm/{id}")
    public String confirmBooking(@PathVariable Long id, RedirectAttributes ra) {
        try {
            bookingService.confirmBooking(id);
            ra.addFlashAttribute("successMsg", "Booking confirmed.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        }
        return "redirect:/admin/bookings";
    }

    @PostMapping("/bookings/reject/{id}")
    public String rejectBooking(@PathVariable Long id,
                                 @RequestParam(defaultValue = "Booking rejected by admin") String reason,
                                 RedirectAttributes ra) {
        bookingService.rejectBooking(id, reason);
        ra.addFlashAttribute("successMsg", "Booking rejected.");
        return "redirect:/admin/bookings";
    }

    @PostMapping("/bookings/complete/{id}")
    public String completeBooking(@PathVariable Long id,
                                   @RequestParam(defaultValue = "0") int lateDays,
                                   RedirectAttributes ra) {
        bookingService.completeBooking(id, lateDays);
        ra.addFlashAttribute("successMsg", "Booking completed.");
        return "redirect:/admin/bookings";
    }

    // ===== Manage Customers =====
    @GetMapping("/customers")
    public String customers(@RequestParam(required = false) String search, Model model) {
        var customers = (search != null && !search.isBlank())
                ? userService.searchCustomers(search)
                : userService.findAllCustomers();
        model.addAttribute("customers", customers);
        model.addAttribute("search", search);
        return "admin/customers";
    }

    // ===== Manage Payments =====
    @GetMapping("/payments")
    public String payments(Model model) {
        model.addAttribute("payments", paymentRepository.findAllByOrderByCreatedAtDesc());
        BigDecimal totalCollected = paymentRepository.getTotalCollected();
        model.addAttribute("totalCollected", totalCollected != null ? totalCollected : BigDecimal.ZERO);
        return "admin/payments";
    }

    // ===== Reports =====
    @GetMapping("/reports")
    public String reports(Model model) {
        model.addAttribute("totalVehicles", vehicleService.count());
        model.addAttribute("totalCustomers", userService.countCustomers());
        model.addAttribute("totalBookings", bookingService.findAll().size());
        BigDecimal totalRevenue = bookingService.getTotalRevenue();
        model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);
        model.addAttribute("completedCount", bookingService.countByStatus(Booking.BookingStatus.COMPLETED));
        model.addAttribute("cancelledCount", bookingService.countByStatus(Booking.BookingStatus.CANCELLED));
        return "admin/reports";
    }
}

