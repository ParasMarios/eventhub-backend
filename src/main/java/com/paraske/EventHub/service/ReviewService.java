package com.paraske.EventHub.service;

import com.paraske.EventHub.model.Review;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.paraske.EventHub.repository.ReviewRepository;

import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    public Review addReview(Review review) {
        // Εδώ θα μπορούσαμε να προσθέσουμε έλεγχο αν το event έχει ήδη τελειώσει
        return reviewRepository.save(review);
    }

    public List<Review> getReviewsByEvent(Long eventId) {
        return reviewRepository.findByEventId(eventId);
    }
}
