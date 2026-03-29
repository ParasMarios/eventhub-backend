package com.paraske.EventHub.repository;

import com.paraske.EventHub.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    @Query("SELECT e FROM Event e WHERE " +
            "(:title IS NULL OR LOWER(e.title) LIKE :title) AND " +
            "(:location IS NULL OR LOWER(e.location) LIKE :location) AND " +
            "(e.dateTime >= :startDate) AND " +
            "(e.dateTime <= :endDate)")
    List<Event> searchEvents(@Param("title") String title,
                             @Param("location") String location,
                             @Param("startDate") LocalDateTime startDate,
                             @Param("endDate") LocalDateTime endDate);
}
