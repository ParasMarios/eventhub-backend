package com.paraske.EventHub.controller;

import com.paraske.EventHub.service.EventService;
import com.paraske.EventHub.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @Autowired
    private EventService eventService;

    @PostMapping("/upload/{eventId}/user/{userId}")
    public ResponseEntity<String> uploadOrganizerMedia(
            @PathVariable Long eventId,
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file) {

        if (!eventService.isOrganizer(eventId, userId)) {
            return ResponseEntity.status(403).body("Error: You are not the organizer of this event!");
        }

        try {
            String filePath = mediaService.saveFile(file);
            return ResponseEntity.ok("Media uploaded successfully! Path: " + filePath);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uploading media: " + e.getMessage());
        }
    }
}