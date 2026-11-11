-- DriveGo Demo Seed Data

-- 2 Cars
INSERT INTO vehicles (vehicle_type, brand, model, registration_number, price_per_day, security_deposit, seats, fuel_type, transmission, description, image_url, number_of_doors, active)
SELECT 'CAR', 'Maruti', 'Swift', 'MH-01-AB-1234', 2500.00, 5000.00, 5, 'Petrol', 'Manual', 'Reliable and fuel-efficient hatchback, ideal for city and highway travel.', '/images/swift.svg', 4, TRUE
WHERE NOT EXISTS (SELECT 1 FROM vehicles WHERE registration_number = 'MH-01-AB-1234');

INSERT INTO vehicles (vehicle_type, brand, model, registration_number, price_per_day, security_deposit, seats, fuel_type, transmission, description, image_url, number_of_doors, active)
SELECT 'CAR', 'Hyundai', 'i20', 'MH-02-CD-5678', 3000.00, 5000.00, 5, 'Petrol', 'Automatic', 'Premium automatic hatchback with touchscreen and sunroof.', '/images/car.svg', 4, TRUE
WHERE NOT EXISTS (SELECT 1 FROM vehicles WHERE registration_number = 'MH-02-CD-5678');

-- 2 Bikes
INSERT INTO vehicles (vehicle_type, brand, model, registration_number, price_per_day, security_deposit, seats, fuel_type, transmission, description, image_url, bike_type, active)
SELECT 'BIKE', 'Royal Enfield', 'Classic 350', 'MH-03-EF-9012', 1200.00, 3000.00, 2, 'Petrol', 'Manual', 'Legendary 350cc cruiser bike for highway touring.', '/images/bike.svg', 'Cruiser', TRUE
WHERE NOT EXISTS (SELECT 1 FROM vehicles WHERE registration_number = 'MH-03-EF-9012');

INSERT INTO vehicles (vehicle_type, brand, model, registration_number, price_per_day, security_deposit, seats, fuel_type, transmission, description, image_url, bike_type, active)
SELECT 'BIKE', 'Honda', 'Activa 6G', 'MH-04-GH-3456', 800.00, 2000.00, 2, 'Petrol', 'Automatic', 'Effortless automatic scooter for local city errands.', '/images/bike.svg', 'Scooter', TRUE
WHERE NOT EXISTS (SELECT 1 FROM vehicles WHERE registration_number = 'MH-04-GH-3456');

-- 2 SUVs
INSERT INTO vehicles (vehicle_type, brand, model, registration_number, price_per_day, security_deposit, seats, fuel_type, transmission, description, image_url, four_wheel_drive, active)
SELECT 'SUV', 'Mahindra', 'Thar 4x4', 'MH-05-IJ-7890', 5000.00, 8000.00, 4, 'Diesel', 'Manual', 'Rugged 4WD adventure off-roader with convertible top.', '/images/suv.svg', TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM vehicles WHERE registration_number = 'MH-05-IJ-7890');

INSERT INTO vehicles (vehicle_type, brand, model, registration_number, price_per_day, security_deposit, seats, fuel_type, transmission, description, image_url, four_wheel_drive, active)
SELECT 'SUV', 'Hyundai', 'Creta', 'MH-06-KL-1234', 4500.00, 7000.00, 5, 'Petrol', 'Automatic', 'Spacious 5-seater luxury SUV with ventilated seats and cruise control.', '/images/suv.svg', FALSE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM vehicles WHERE registration_number = 'MH-06-KL-1234');

