package com.paraske.EventHub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paraske.EventHub.dto.EventRatingStats;
import com.paraske.EventHub.model.Event;
import com.paraske.EventHub.service.EventService;
import com.paraske.EventHub.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @Autowired
    private MediaService mediaService;

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<Event> createEvent(
            @RequestParam("event") String eventJson,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        // 1. Μετατροπή του String JSON σε αντικείμενο Event
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        Event event = objectMapper.readValue(eventJson, Event.class);

        // 2. Αποθήκευση αρχείου και λήψη ονόματος
        String fileName = mediaService.saveFile(file);

        // 3. Ενημέρωση του imageUrl στο event
        event.setImageUrl(fileName);

        // 4. Αποθήκευση στη βάση
        return ResponseEntity.ok(eventService.createEvent(event));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(
            @PathVariable Long id,
            @RequestBody Event eventDetails,
            Authentication authentication) {

        String currentUsername = authentication.getName();
        return ResponseEntity.ok(eventService.updateEvent(id, eventDetails, currentUsername));
    }

    @PostMapping("/{eventId}/join")
    public ResponseEntity<?> joinEvent(@PathVariable Long eventId, Authentication authentication) {
        String username = authentication.getName();
        eventService.joinEvent(eventId, username);
        return ResponseEntity.ok("Joined successfully");
    }

    @GetMapping("/joined/{userId}")
    public ResponseEntity<Set<Event>> getJoinedEvents(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.getJoinedEventsByUser(userId));
    }

    @GetMapping("/{eventId}/stats")
    public ResponseEntity<EventRatingStats> getEventStats(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventStats(eventId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Event>> getEventsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(eventService.getEventsByOrganizer(userId));
    }

    @GetMapping("/filter")
    public List<Event> getFilteredEvents(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return eventService.filterEvents(title, location, start, end);
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long eventId) {
        // 1. Παίρνουμε τον συνδεδεμένο χρήστη (από το context που έστησε το JwtFilter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // Επιστρέφει το 'subject' (username) του token

        // 2. Καλούμε το Service για να κάνει τη διαγραφή (και τον έλεγχο ασφαλείας)
        try {
            eventService.secureDeleteEvent(eventId, currentUsername);
            return ResponseEntity.ok("Event deleted successfully");
        } catch (AccessDeniedException ade) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Error: " + ade.getMessage());
        } catch (RuntimeException re) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Error: " + re.getMessage());
        }
    }
}
