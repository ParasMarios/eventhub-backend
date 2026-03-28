package com.paraske.EventHub.service;

import com.paraske.EventHub.dto.EventRatingStats;
import com.paraske.EventHub.model.Event;
import com.paraske.EventHub.model.Review;
import com.paraske.EventHub.model.User;
import com.paraske.EventHub.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.paraske.EventHub.repository.EventRepository;
import com.paraske.EventHub.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
        Long organizerId = event.getOrganizer().getId();

        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + organizerId));

        event.setOrganizer(organizer);

        return eventRepository.save(event);
    }

    public Event updateEvent(Long id, Event details, String username) {
        Event event = eventRepository.findById(id).orElseThrow();

        // Έλεγχος αν είναι ο ιδιοκτήτης
        if (!event.getOrganizer().getUsername().equals(username)) {
            throw new AccessDeniedException("Not authorized");
        }

        event.setTitle(details.getTitle());
        event.setDescription(details.getDescription());
        event.setLocation(details.getLocation());
        event.setDateTime(details.getDateTime());

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
            return new EventRatingStats(0.0,  0L);
        }

        double overall = reviews.stream().mapToInt(Review::getOverallRating).average().orElse(0.0);

        return new EventRatingStats(overall, (long) reviews.size());
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

    public Optional<Event> getEventById(Long id) {
        return eventRepository.findById(id);
    }

    public List<Event> getEventsByOrganizer(Long userId) {
        return eventRepository.findByOrganizerId(userId);
        }

    public void secureDeleteEvent(Long eventId, String currentUsername) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        //Συγκρίνουμε το username του organizer με το συνδεδεμένο username
        if (!event.getOrganizer().getUsername().equals(currentUsername)) {
            throw new AccessDeniedException("You are not the organizer of this event! You cannot delete it.");
        }

        eventRepository.deleteById(eventId);
    }

}