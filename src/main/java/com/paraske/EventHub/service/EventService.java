package com.paraske.EventHub.service;

import com.paraske.EventHub.dto.EventRatingStats;
import com.paraske.EventHub.model.Event;
import com.paraske.EventHub.model.Review;
import com.paraske.EventHub.model.User;
import com.paraske.EventHub.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.paraske.EventHub.repository.EventRepository;
import com.paraske.EventHub.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    public Event createEvent(Event event) {
        return eventRepository.save(event);
    }

    @Transactional
    public void joinEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        event.getParticipants().add(user);

        eventRepository.save(event);
    }

    public EventRatingStats getEventStats(Long eventId) {
        List<Review> reviews = reviewRepository.findByEventId(eventId);

        if (reviews.isEmpty()) {
            return new EventRatingStats(0.0, 0.0, 0.0, 0.0, 0L);
        }

        double overall = reviews.stream().mapToInt(Review::getOverallRating).average().orElse(0.0);

        double org = reviews.stream()
                .filter(r -> r.getOrganizationRating() != null)
                .mapToInt(Review::getOrganizationRating).average().orElse(0.0);

        double content = reviews.stream()
                .filter(r -> r.getContentRating() != null)
                .mapToInt(Review::getContentRating).average().orElse(0.0);

        double venue = reviews.stream()
                .filter(r -> r.getVenueRating() != null)
                .mapToInt(Review::getVenueRating).average().orElse(0.0);

        return new EventRatingStats(overall, org, content, venue, (long) reviews.size());
    }

    public List<Event> filterEvents(String title, String location, LocalDateTime start, LocalDateTime end) {

        if (start == null) start = LocalDateTime.now();
        if (end == null) end = LocalDateTime.now().plusYears(75);

        String searchTitle = (title == null) ? "" : title;
        String searchLocation = (location == null) ? "" : location;

        return eventRepository.findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCaseAndDateTimeBetween(
                searchTitle, searchLocation, start, end
        );
    }

    public boolean isOrganizer(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        return event.getOrganizer().getId().equals(userId);
    }
}