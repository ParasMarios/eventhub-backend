package com.paraske.EventHub.repository;

import com.paraske.EventHub.model.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EventImageRepository extends JpaRepository<EventImage, Long> {
    List<EventImage> findByEventId(Long eventId);
}
