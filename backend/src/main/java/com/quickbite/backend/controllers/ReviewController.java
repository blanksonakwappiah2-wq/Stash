package com.quickbite.backend.controllers;

import com.quickbite.backend.entities.Review;
import com.quickbite.backend.repositories.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('MANAGER')")
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER')")
    public Review createReview(@RequestBody Review review) {
        return reviewRepository.save(review);
    }
}
