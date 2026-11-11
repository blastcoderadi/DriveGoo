package com.drivego;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DriveGo – Vehicle Rental Management System
 * Entry point for the Spring Boot application.
 *
 * To run with H2 (no MySQL needed):
 *   mvn spring-boot:run -Dspring-boot.run.profiles=dev
 *
 * To run with MySQL:
 *   mvn spring-boot:run
 */
@SpringBootApplication
public class DriveGoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DriveGoApplication.class, args);
        System.out.println("=========================================");
        System.out.println("  DriveGo is running at http://localhost:8080");
        System.out.println("  Demo admin: use a Google account listed");
        System.out.println("  in drivego.admin-emails property");
        System.out.println("=========================================");
    }
}
