package com.paraske.EventHub.controller;

import com.paraske.EventHub.model.Review;
import com.paraske.EventHub.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        Review savedReview = reviewService.addReview(review);
        return ResponseEntity.ok(savedReview);
    }

    @GetMapping("/event/{eventId}")
    public List<Review> getReviewsByEvent(@PathVariable Long eventId) {
        return reviewService.getReviewsByEvent(eventId);
    }
}
