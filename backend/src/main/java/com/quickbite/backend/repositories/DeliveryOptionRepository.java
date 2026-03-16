package com.quickbite.backend.repositories;

import com.quickbite.backend.entities.DeliveryOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryOptionRepository extends JpaRepository<DeliveryOption, Long> {
}