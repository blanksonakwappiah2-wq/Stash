package com.quickbite.backend.services;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class DistanceService {

    @Value("${google.maps.api.key:YOUR_GOOGLE_MAPS_API_KEY}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    
    @Cacheable("distances")
    public double getDistance(String origin, String destination) {
        String url = UriComponentsBuilder.fromHttpUrl("https://maps.googleapis.com/maps/api/distancematrix/json")
                .queryParam("origins", origin)
                .queryParam("destinations", destination)
                .queryParam("units", "metric")
                .queryParam("key", apiKey)
                .toUriString();

        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);
        if (response != null && "OK".equals(response.get("status"))) {
            var rows = (java.util.List<Map<String, Object>>) response.get("rows");
            if (rows != null && !rows.isEmpty()) {
                var elements = (java.util.List<Map<String, Object>>) rows.get(0).get("elements");
                if (elements != null && !elements.isEmpty()) {
                    var element = elements.get(0);
                    if ("OK".equals(element.get("status"))) {
                        var distance = (Map<String, Object>) element.get("distance");
                        if (distance != null) {
                            return ((Number) distance.get("value")).doubleValue() / 1000.0; // meters to km
                        }
                    }
                }
            }
        }
        throw new RuntimeException("Unable to calculate distance");
    }
}