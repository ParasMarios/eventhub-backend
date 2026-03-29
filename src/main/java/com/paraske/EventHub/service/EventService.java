package com.paraske.EventHub.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.paraske.EventHub.dto.EventRatingStats;
import com.paraske.EventHub.model.Event;
import com.paraske.EventHub.model.EventImage;
import com.paraske.EventHub.model.Review;
import com.paraske.EventHub.model.User;
import com.paraske.EventHub.repository.EventImageRepository;
import com.paraske.EventHub.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.paraske.EventHub.repository.EventRepository;
import com.paraske.EventHub.repository.UserRepository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class EventService {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EventImageRepository eventImageRepository;

    @Autowired
    private Cloudinary cloudinary;


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
    public void joinEvent(Long eventId, String username) {
        Event event = eventRepository.findById(eventId).orElseThrow();
        User user = userRepository.findByUsername(username).orElseThrow();

        event.getParticipants().add(user);
        eventRepository.save(event);
    }

    public Set<Event> getJoinedEventsByUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return user.getJoinedEvents();
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

    public List<EventImage> uploadGalleryImages(Long eventId, MultipartFile[] files, User currentUser) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Το Event δεν βρέθηκε."));

        List<EventImage> uploadedImages = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                // 1. Ανέβασμα της εικόνας στο Cloudinary
                Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

                String imageUrl = uploadResult.get("secure_url").toString();

                EventImage image = new EventImage(imageUrl, event, currentUser);
                uploadedImages.add(eventImageRepository.save(image));

            } catch (IOException e) {
                System.err.println("Αποτυχία ανεβάσματος στο Cloudinary: " + e.getMessage());
                throw new RuntimeException("Αποτυχία αποθήκευσης της εικόνας.");
            }
        }
        return uploadedImages;
    }

    @Transactional
    public void deleteGalleryImage(Long imageId, User currentUser) {
        EventImage image = eventImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Η φωτογραφία δεν βρέθηκε."));

        boolean isUploader = image.getUploader().getId().equals(currentUser.getId());
        boolean isOrganizer = image.getEvent().getOrganizer().getId().equals(currentUser.getId());

        if (!isUploader && !isOrganizer) {
            throw new RuntimeException("Δεν έχετε δικαίωμα να διαγράψετε αυτή τη φωτογραφία.");
        }

        eventImageRepository.delete(image);
    }

}