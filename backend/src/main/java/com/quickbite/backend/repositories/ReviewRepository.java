package com.quickbite.backend.repositories;

import com.quickbite.backend.entities.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}