package com.paraske.EventHub.repository;

import com.paraske.EventHub.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    // Custom μέθοδος για αναζήτηση βάσει τοποθεσίας
    List<Event> findByLocationContainingIgnoreCase(String location);

    // Εύρεση events που οργάνωσε συγκεκριμένος χρήστης
    List<Event> findByOrganizerId(Long userId);

    List<Event> findByTitleContainingIgnoreCaseAndLocationContainingIgnoreCaseAndDateTimeBetween(
            String title,
            String location,
            LocalDateTime start,
            LocalDateTime end
    );
}
