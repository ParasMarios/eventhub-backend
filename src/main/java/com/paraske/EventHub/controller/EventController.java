package com.paraske.EventHub.controller;

import com.paraske.EventHub.dto.EventRatingStats;
import com.paraske.EventHub.model.Event;
import com.paraske.EventHub.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    @Autowired
    private EventService eventService;

    @GetMapping
    public List<Event> getAllEvents() {
        return eventService.getAllEvents();
    }

    @PostMapping
    public Event createEvent(@RequestBody Event event) {
        return eventService.createEvent(event);
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
        return eventService.getEventById(id) // Βεβαιώσου ότι έχεις αυτή τη μέθοδο στο Service
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
