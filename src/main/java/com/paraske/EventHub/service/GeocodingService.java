package com.paraske.EventHub.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class GeocodingService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Μετατρέπει π.χ. το "Τρίπολη" σε [37.508, 22.373]
    public double[] getCoordinates(String locationName) {
        try {
            String url = "https://nominatim.openstreetmap.org/search?q=" + locationName + "&format=json&limit=1";

            // Το OpenStreetMap απαιτεί ένα User-Agent header για να μην μας μπλοκάρει
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "EventHubApp/1.0");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());
            if (root.isArray() && root.size() > 0) {
                JsonNode firstResult = root.get(0);
                double lat = firstResult.get("lat").asDouble();
                double lon = firstResult.get("lon").asDouble();
                return new double[]{lat, lon};
            }
        } catch (Exception e) {
            System.err.println("Αποτυχία γεωεντοπισμού για: " + locationName);
        }
        return null;
    }
}
