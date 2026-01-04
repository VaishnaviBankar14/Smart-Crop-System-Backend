package com.smartcrops.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class GeoCodingService {

    @Value("${weather.api.key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public double[] getLatLon(String place) {

        String url =
            "https://api.openweathermap.org/geo/1.0/direct?q="
            + place + ",IN&limit=1&appid=" + apiKey;

        try {
            Object[] response = restTemplate.getForObject(url, Object[].class);

            // ✅ IF LOCATION NOT FOUND → FALLBACK (NO ERROR)
            if (response == null || response.length == 0) {
                return fallbackLatLon();
            }

            Map<String, Object> location = (Map<String, Object>) response[0];

            double lat = ((Number) location.get("lat")).doubleValue();
            double lon = ((Number) location.get("lon")).doubleValue();

            return new double[]{lat, lon};

        } catch (Exception e) {
            // ✅ FINAL SAFETY NET
            return fallbackLatLon();
        }
    }

    // ✅ SAFE DEFAULT LOCATION (Pune – Maharashtra)
    private double[] fallbackLatLon() {
        return new double[]{18.5204, 73.8567};
    }
}
