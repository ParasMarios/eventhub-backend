package com.paraske.EventHub.service;

import com.paraske.EventHub.model.Event;
import com.paraske.EventHub.model.Review;
import com.paraske.EventHub.model.User;
import com.paraske.EventHub.repository.EventRepository;
import com.paraske.EventHub.repository.ReviewRepository;
import com.paraske.EventHub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EventRepository eventRepository;

    @Test
    void addReview_shouldAddReview_whenUserAndEventExist() {
        User author = new User();
        author.setId(1L);
        Event event = new Event();
        event.setId(1L);
        Review review = new Review();
        review.setAuthor(author);
        review.setEvent(event);

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        Review result = reviewService.addReview(review);

        assertNotNull(result);
        assertEquals(author, result.getAuthor());
        assertEquals(event, result.getEvent());
    }

    @Test
    void addReview_shouldThrowException_whenUserNotFound() {
        User author = new User();
        author.setId(1L);
        Event event = new Event();
        event.setId(1L);
        Review review = new Review();
        review.setAuthor(author);
        review.setEvent(event);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reviewService.addReview(review));
    }

    @Test
    void addReview_shouldThrowException_whenEventNotFound() {
        User author = new User();
        author.setId(1L);
        Event event = new Event();
        event.setId(1L);
        Review review = new Review();
        review.setAuthor(author);
        review.setEvent(event);

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(eventRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> reviewService.addReview(review));
    }

    @Test
    void getReviewsByEvent_shouldReturnReviews_whenEventExists() {
        Event event = new Event();
        event.setId(1L);
        Review review = new Review();
        review.setEvent(event);

        when(reviewRepository.findByEventId(1L)).thenReturn(Collections.singletonList(review));

        List<Review> result = reviewService.getReviewsByEvent(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(event.getId(), result.get(0).getEvent().getId());
    }
}