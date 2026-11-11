package com.drivego.config;

import com.drivego.model.*;
import com.drivego.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.math.BigDecimal;

/**
 * DataSeeder seeds the database with one demo admin and 6 vehicles on startup.
 * This runs only if the vehicles table is empty.
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    public CommandLineRunner seedData(UserRepository userRepo, VehicleRepository vehicleRepo) {
        return args -> {
            // Seed vehicles if none exist
            if (vehicleRepo.count() == 0) {
                log.info("Seeding demo data...");

                // 2 Cars
                Car car1 = new Car("Maruti", "Swift", "MH-01-AB-1234",
                        new BigDecimal("2500"), new BigDecimal("5000"), 4);
                car1.setSeats(5); car1.setFuelType("Petrol"); car1.setTransmission("Manual");
                car1.setDescription("Compact hatchback, great for city drives.");
                car1.setImageUrl("/images/swift.svg");

                Car car2 = new Car("Hyundai", "i20", "MH-02-CD-5678",
                        new BigDecimal("3000"), new BigDecimal("5000"), 4);
                car2.setSeats(5); car2.setFuelType("Petrol"); car2.setTransmission("Automatic");
                car2.setDescription("Premium hatchback with automatic transmission.");
                car2.setImageUrl("/images/car.svg");

                // 2 Bikes
                Bike bike1 = new Bike("Royal Enfield", "Classic 350", "MH-03-EF-9012",
                        new BigDecimal("1200"), new BigDecimal("3000"), "Cruiser");
                bike1.setSeats(2); bike1.setFuelType("Petrol"); bike1.setTransmission("Manual");
                bike1.setDescription("Iconic cruiser for scenic road trips.");
                bike1.setImageUrl("/images/bike.svg");

                Bike bike2 = new Bike("Honda", "Activa 6G", "MH-04-GH-3456",
                        new BigDecimal("800"), new BigDecimal("2000"), "Scooter");
                bike2.setSeats(2); bike2.setFuelType("Petrol"); bike2.setTransmission("Automatic");
                bike2.setDescription("Fuel-efficient scooter for city commuting.");
                bike2.setImageUrl("/images/bike.svg");

                // 2 SUVs
                SUV suv1 = new SUV("Mahindra", "Thar", "MH-05-IJ-7890",
                        new BigDecimal("5000"), new BigDecimal("8000"), true);
                suv1.setSeats(4); suv1.setFuelType("Diesel"); suv1.setTransmission("Manual");
                suv1.setDescription("Off-road capable 4WD SUV for adventure seekers.");
                suv1.setImageUrl("/images/suv.svg");

                SUV suv2 = new SUV("Hyundai", "Creta", "MH-06-KL-1234",
                        new BigDecimal("4500"), new BigDecimal("7000"), false);
                suv2.setSeats(5); suv2.setFuelType("Petrol"); suv2.setTransmission("Automatic");
                suv2.setDescription("Popular family SUV with premium features.");
                suv2.setImageUrl("/images/suv.svg");

                vehicleRepo.save(car1);
                vehicleRepo.save(car2);
                vehicleRepo.save(bike1);
                vehicleRepo.save(bike2);
                vehicleRepo.save(suv1);
                vehicleRepo.save(suv2);

                log.info("Seeded 6 vehicles: 2 Cars, 2 Bikes, 2 SUVs");
            }

            // Seed demo admin (SRM viva and grading demo)
            if (userRepo.findByEmail("admin@drivego.com").isEmpty()) {
                Admin admin = new Admin("DriveGo Administrator", "admin@drivego.com", "9999999999", User.AuthProvider.GOOGLE);
                admin.setDepartment("Fleet Operations");
                userRepo.save(admin);
                log.info("Seeded demo admin: admin@drivego.com / 9999999999");
            }
            log.info("DriveGo data seeding complete.");
        };
    }
}
