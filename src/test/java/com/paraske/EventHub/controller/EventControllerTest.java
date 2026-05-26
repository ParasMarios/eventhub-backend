package com.paraske.EventHub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paraske.EventHub.dto.EventRatingStats;
import com.paraske.EventHub.model.Event;
import com.paraske.EventHub.model.User;
import com.paraske.EventHub.repository.EventRepository;
import com.paraske.EventHub.repository.UserRepository;
import com.paraske.EventHub.service.EventService;
import com.paraske.EventHub.service.MediaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EventService eventService;

    @MockBean
    private MediaService mediaService;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getAllEvents_shouldReturnListOfEvents() throws Exception {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Test Event");

        when(eventService.filterEvents(any(), any(), any(), any(), any())).thenReturn(Collections.singletonList(event));

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Event"));
    }

    @Test
    @WithMockUser
    void createEvent_shouldCreateEvent() throws Exception {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Test Event");

        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "test".getBytes());
        String eventJson = objectMapper.writeValueAsString(event);

        when(mediaService.saveFile(any())).thenReturn("test-url");
        when(eventService.createEvent(any(Event.class))).thenReturn(event);

        mockMvc.perform(multipart("/api/events")
                        .file(file)
                        .param("event", eventJson)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Event"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateEvent_shouldUpdateEvent() throws Exception {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Updated Event");

        when(eventService.updateEvent(any(Long.class), any(Event.class), anyString())).thenReturn(event);

        mockMvc.perform(put("/api/events/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(event)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Event"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void joinEvent_shouldJoinEvent() throws Exception {
        doNothing().when(eventService).joinEvent(any(Long.class), anyString());

        mockMvc.perform(post("/api/events/1/join")
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void getJoinedEvents_shouldReturnJoinedEvents() throws Exception {
        Event event = new Event();
        event.setId(1L);
        User user = new User();
        user.setId(1L);
        user.setJoinedEvents(new HashSet<>(Collections.singletonList(event)));

        when(eventService.getJoinedEventsByUser(1L)).thenReturn(user.getJoinedEvents());

        mockMvc.perform(get("/api/events/joined/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @WithMockUser
    void getEventStats_shouldReturnEventStats() throws Exception {
        EventRatingStats stats = new EventRatingStats(4.5, 10L);

        when(eventService.getEventStats(1L)).thenReturn(stats);

        mockMvc.perform(get("/api/events/1/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageOverall").value(4.5))
                .andExpect(jsonPath("$.totalReviews").value(10L));
    }

    @Test
    @WithMockUser
    void getEventById_shouldReturnEvent() throws Exception {
        Event event = new Event();
        event.setId(1L);

        when(eventService.getEventById(1L)).thenReturn(Optional.of(event));

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser
    void getEventsByUser_shouldReturnEvents() throws Exception {
        Event event = new Event();
        event.setId(1L);

        when(eventService.getEventsByOrganizer(1L)).thenReturn(Collections.singletonList(event));

        mockMvc.perform(get("/api/events/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @WithMockUser(username = "testuser")
    void deleteEvent_shouldDeleteEvent() throws Exception {
        doNothing().when(eventService).secureDeleteEvent(any(Long.class), anyString());

        mockMvc.perform(delete("/api/events/1")
                        .with(csrf()))
                .andExpect(status().isOk());
    }
}