package com.drivego.controller;

import com.drivego.service.VehicleService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final VehicleService vehicleService;

    public HomeController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @GetMapping("/")
    public String home(Model model) {
        var allVehicles = vehicleService.findAvailable();
        int limit = Math.min(6, allVehicles.size());
        model.addAttribute("featuredVehicles", allVehicles.subList(0, limit));
        model.addAttribute("categories", vehicleService.getCategories());
        model.addAttribute("totalVehicles", vehicleService.count());
        return "index";
    }

    @GetMapping("/about")
    public String about() {
        return "about";
    }
}

