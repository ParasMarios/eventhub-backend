package com.paraske.EventHub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paraske.EventHub.model.User;
import com.paraske.EventHub.repository.EventRepository;
import com.paraske.EventHub.repository.ReviewRepository;
import com.paraske.EventHub.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private EventRepository eventRepository;

    @MockBean
    private ReviewRepository reviewRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findAll()).thenReturn(Collections.singletonList(user));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"));
    }

    @Test
    @WithMockUser
    void createUser_shouldCreateUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("newuser");

        when(userRepository.save(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @WithMockUser
    void getUserById_shouldReturnUser_whenUserExists() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @WithMockUser
    void getPublicProfile_shouldReturnPublicProfile() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findByOrganizerId(1L)).thenReturn(Collections.emptyList());
        when(reviewRepository.getAverageRatingForOrganizer(1L)).thenReturn(4.5);

        mockMvc.perform(get("/api/users/1/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.averageRating").value(4.5));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateProfile_shouldUpdateProfile_whenUserIsTheSame() throws Exception {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setUsername("testuser");

        User updatedData = new User();
        updatedData.setDescription("New Description");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(currentUser));

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "anotheruser")
    void updateProfile_shouldReturnForbidden_whenUserIsNotTheSame() throws Exception {
        User currentUser = new User();
        currentUser.setId(2L);
        currentUser.setUsername("anotheruser");

        User updatedData = new User();
        updatedData.setDescription("New Description");

        when(userRepository.findByUsername("anotheruser")).thenReturn(Optional.of(currentUser));

        mockMvc.perform(put("/api/users/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedData)))
                .andExpect(status().isForbidden());
    }
}