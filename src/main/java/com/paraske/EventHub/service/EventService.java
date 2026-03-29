package com.paraske.EventHub.service;

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
        // 1. Βρίσκουμε το Event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Το Event δεν βρέθηκε!"));

        List<EventImage> savedImages = new ArrayList<>();

        String uploadDir = "uploads/";

        try {
            // Σιγουρευόμαστε ότι ο φάκελος υπάρχει
            Files.createDirectories(Paths.get(uploadDir));

            // 2. Επεξεργαζόμαστε κάθε αρχείο ξεχωριστά
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    // Φτιάχνουμε ένα μοναδικό όνομα για να μην "πατήσει" η μία φωτό την άλλη
                    String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                    Path filePath = Paths.get(uploadDir, filename);

                    // Αποθήκευση στον δίσκο
                    Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                    // 3. Δημιουργία και αποθήκευση της οντότητας στη Βάση
                    EventImage image = new EventImage(filename, event, currentUser);
                    savedImages.add(eventImageRepository.save(image));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Αποτυχία αποθήκευσης των αρχείων", e);
        }

        return savedImages;
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

        String uploadDir = "uploads/";
        Path filePath = Paths.get(uploadDir, image.getImageUrl());

        try {
            Files.deleteIfExists(filePath);
            System.out.println("Το αρχείο διαγράφηκε από τον δίσκο: " + filePath);
        } catch (IOException e) {
            System.err.println("Αποτυχία διαγραφής αρχείου: " + e.getMessage());
        }

        eventImageRepository.delete(image);
    }

}