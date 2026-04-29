package com.paraske.EventHub.repository;

import com.paraske.EventHub.model.Event;
import com.paraske.EventHub.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByEventId(Long eventId);

    @Query("SELECT AVG(r.overallRating) FROM Review r WHERE r.event.organizer.id = :organizerId")
    Double getAverageRatingForOrganizer(@Param("organizerId") Long organizerId);
}
