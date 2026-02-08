package com.paraske.EventHub.controller;

import com.paraske.EventHub.model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.paraske.EventHub.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:3000")
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
}
