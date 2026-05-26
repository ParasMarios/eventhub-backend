package com.paraske.EventHub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeocodingServiceTest {

    @InjectMocks
    private GeocodingService geocodingService;

    @Mock
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(geocodingService, "restTemplate", restTemplate);
        ReflectionTestUtils.setField(geocodingService, "objectMapper", objectMapper);
    }

    @Test
    void getCoordinates_shouldReturnCoordinates_whenLocationIsValid() {
        String locationName = "Paris";
        String jsonResponse = "[{\"lat\":\"48.8588443\",\"lon\":\"2.2943506\"}]";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);

        double[] coordinates = geocodingService.getCoordinates(locationName);

        assertNotNull(coordinates);
        assertEquals(48.8588443, coordinates[0]);
        assertEquals(2.2943506, coordinates[1]);
    }

    @Test
    void getCoordinates_shouldReturnNull_whenLocationIsNotFound() {
        String locationName = "InvalidLocation";
        String jsonResponse = "[]";
        ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonResponse, HttpStatus.OK);

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenReturn(responseEntity);

        double[] coordinates = geocodingService.getCoordinates(locationName);

        assertNull(coordinates);
    }

    @Test
    void getCoordinates_shouldReturnNull_whenApiCallFails() {
        String locationName = "Paris";

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), any(Class.class)))
                .thenThrow(new RuntimeException("API call failed"));

        double[] coordinates = geocodingService.getCoordinates(locationName);

        assertNull(coordinates);
    }
}