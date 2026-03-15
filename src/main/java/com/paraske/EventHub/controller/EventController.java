package com.paraske.EventHub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.paraske.EventHub.dto.EventRatingStats;
import com.paraske.EventHub.model.Event;
import com.paraske.EventHub.service.EventService;
import com.paraske.EventHub.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

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

    @PostMapping("/{eventId}/join/{userId}")
    public String joinEvent(@PathVariable Long eventId, @PathVariable Long userId) {
        try {
            eventService.joinEvent(eventId, userId);
            return "Successfully joined event!";
        } catch (Exception e) {
            return "Error joining event: " + e.getMessage();
        }
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

    @GetMapping("/filter")
    public List<Event> getFilteredEvents(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        return eventService.filterEvents(title, location, start, end);
    }
}
