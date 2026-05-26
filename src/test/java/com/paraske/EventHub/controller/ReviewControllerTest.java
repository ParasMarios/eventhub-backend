package com.paraske.EventHub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paraske.EventHub.model.Event;
import com.paraske.EventHub.model.Review;
import com.paraske.EventHub.model.User;
import com.paraske.EventHub.service.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReviewController.class)
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void createReview_shouldCreateReview() throws Exception {
        User author = new User();
        author.setId(1L);
        Event event = new Event();
        event.setId(1L);
        Review review = new Review();
        review.setAuthor(author);
        review.setEvent(event);
        review.setOverallRating(5);

        when(reviewService.addReview(any(Review.class))).thenReturn(review);

        mockMvc.perform(post("/api/reviews")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(review)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.overallRating").value(5));
    }

    @Test
    @WithMockUser
    void getReviewsByEvent_shouldReturnReviews() throws Exception {
        Event event = new Event();
        event.setId(1L);
        Review review = new Review();
        review.setEvent(event);

        when(reviewService.getReviewsByEvent(1L)).thenReturn(Collections.singletonList(review));

        mockMvc.perform(get("/api/reviews/event/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].event.id").value(1L));
    }
}