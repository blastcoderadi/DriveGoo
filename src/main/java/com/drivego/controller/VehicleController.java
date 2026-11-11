package com.drivego.controller;

import com.drivego.exception.VehicleNotAvailableException;
import com.drivego.service.VehicleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@Controller
@RequestMapping("/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping
    public String listVehicles(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "price_asc") String sort,
            Model model) {

        var vehicles = vehicleService.search(search, category, minPrice, maxPrice);
        if ("price_desc".equals(sort)) {
            vehicles.sort((a, b) -> b.getPricePerDay().compareTo(a.getPricePerDay()));
        } else {
            vehicles.sort((a, b) -> a.getPricePerDay().compareTo(b.getPricePerDay()));
        }

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("categories", vehicleService.getCategories());
        model.addAttribute("search", search);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sort", sort);
        model.addAttribute("resultCount", vehicles.size());
        return "vehicle/list";
    }

    @GetMapping("/{id}")
    public String vehicleDetail(@PathVariable Long id, Model model) {
        var vehicle = vehicleService.findById(id)
                .orElseThrow(() -> new VehicleNotAvailableException("Vehicle not found with ID: " + id));
        model.addAttribute("vehicle", vehicle);
        return "vehicle/detail";
    }
}

