package com.paraske.EventHub.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "event_images")
public class EventImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    private Event event;

    public EventImage() {}

    public EventImage(String imageUrl, Event event) {
        this.imageUrl = imageUrl;
        this.event = event;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public Event getEvent() { return event; }

    public void setEvent(Event event) { this.event = event; }
}
