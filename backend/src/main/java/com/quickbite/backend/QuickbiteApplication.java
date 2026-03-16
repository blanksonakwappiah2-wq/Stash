package com.quickbite.backend;

import com.quickbite.backend.entities.DeliveryOption;
import com.quickbite.backend.repositories.DeliveryOptionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class QuickbiteApplication {
    public static void main(String[] args) {
        SpringApplication.run(QuickbiteApplication.class, args);
    }

    @Bean
    CommandLineRunner initDatabase(DeliveryOptionRepository deliveryOptionRepository) {
        return args -> {
            if (deliveryOptionRepository.count() == 0) {
                DeliveryOption standard = new DeliveryOption();
                standard.setMethod("standard");
                standard.setBaseFee(2.99);
                standard.setPerKmFee(0.50);
                standard.setEstimatedTime(45);
                deliveryOptionRepository.save(standard);

                DeliveryOption express = new DeliveryOption();
                express.setMethod("express");
                express.setBaseFee(4.99);
                express.setPerKmFee(0.75);
                express.setEstimatedTime(25);
                deliveryOptionRepository.save(express);

                DeliveryOption drone = new DeliveryOption();
                drone.setMethod("drone");
                drone.setBaseFee(6.99);
                drone.setPerKmFee(1.00);
                drone.setEstimatedTime(15);
                deliveryOptionRepository.save(drone);
            }
        };
    }
}