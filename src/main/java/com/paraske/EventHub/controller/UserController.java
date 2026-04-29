package com.paraske.EventHub.controller;

import com.paraske.EventHub.model.Event;
import com.paraske.EventHub.model.User;
import com.paraske.EventHub.repository.EventRepository;
import com.paraske.EventHub.repository.ReviewRepository;
import com.paraske.EventHub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userRepository.save(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/public")
    public ResponseEntity<Map<String, Object>> getPublicProfile(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ο χρήστης δεν βρέθηκε"));

        List<Event> events = eventRepository.findByOrganizerId(id);

        Double avgRating = reviewRepository.getAverageRatingForOrganizer(id);

        Map<String, Object> response = new HashMap<>();
        response.put("id", user.getId());
        response.put("username", user.getUsername());
        response.put("description", user.getDescription());
        response.put("address", user.getAddress());
        response.put("phone", user.getPhone());
        response.put("publicEmail", user.getPublicEmail());
        response.put("averageRating", avgRating != null ? avgRating : 0.0);
        response.put("events", events);

        return ResponseEntity.ok(response);
    }
}
