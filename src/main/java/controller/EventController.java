package controller;

import model.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import repository.EventRepository;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "http://localhost:3000")
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    // 1. Προβολή όλων των events
    @GetMapping
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    // 2. Δημιουργία νέου event
    @PostMapping
    public Event createEvent(@RequestBody Event event) {
        return eventRepository.save(event);
    }

    // 3. Αναζήτηση ανά τοποθεσία
    @GetMapping("/search")
    public List<Event> searchEvents(@RequestParam String location) {
        return eventRepository.findByLocationContainingIgnoreCase(location);
    }
}
