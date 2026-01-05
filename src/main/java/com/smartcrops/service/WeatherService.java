package com.smartcrops.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class WeatherService {

    @Value("${weather.api.key}")
    private String apiKey;

    // ❌ private final RestTemplate restTemplate = new RestTemplate();
    // ✅ Use timeout-enabled RestTemplate
    @Autowired
    private RestTemplate restTemplate;

    // 🌦 Fetch raw weather data by city
    public Map<String, Object> getWeatherByCity(String city) {

        String url =
            "https://api.openweathermap.org/data/2.5/weather?q="
            + city +
            "&units=metric&appid="
            + apiKey;

        return restTemplate.getForObject(url, Map.class);
    }

    // 🌦 Fetch weather using latitude & longitude
    public Map<String, Object> getWeatherByLatLon(double lat, double lon) {

        String url =
            "https://api.openweathermap.org/data/2.5/weather?lat="
            + lat +
            "&lon=" + lon +
            "&units=metric&appid=" +
            apiKey;

        return restTemplate.getForObject(url, Map.class);
    }

    // 🌡 Extract temperature safely
    public double getTemperature(Map<String, Object> weather) {
        Map<String, Object> main = (Map<String, Object>) weather.get("main");
        return ((Number) main.get("temp")).doubleValue();
    }

    // 🌧 Extract rainfall safely
    public double getRainfall(Map<String, Object> weather) {

        if (weather.get("rain") == null) return 0.0;

        Map<String, Object> rain = (Map<String, Object>) weather.get("rain");
        Object value = rain.get("1h");

        if (value == null) return 0.0;

        return ((Number) value).doubleValue();
    }

    // 🌱 Detect season (Indian context)
    public String detectSeason(double temp, double rainfall) {

        if (temp >= 22) return "Kharif";
        if (temp < 22 && temp >= 10) return "Rabi";
        return "Zaid";
    }

    // 🌦 7-Day Forecast (UNCHANGED – optional usage)
    public double[] get7DayWeatherAverages(double lat, double lon) {

        String url =
            "https://api.openweathermap.org/data/2.5/forecast?lat="
            + lat +
            "&lon=" + lon +
            "&units=metric&appid=" +
            apiKey;

        Map<String, Object> response =
                restTemplate.getForObject(url, Map.class);

        List<Map<String, Object>> list =
                (List<Map<String, Object>>) response.get("list");

        double totalRain = 0.0;
        double totalTemp = 0.0;
        int count = 0;

        for (Map<String, Object> item : list) {

            Map<String, Object> main =
                    (Map<String, Object>) item.get("main");
            totalTemp += ((Number) main.get("temp")).doubleValue();

            if (item.get("rain") != null) {
                Map<String, Object> rain =
                        (Map<String, Object>) item.get("rain");
                Object value = rain.get("3h");
                if (value != null) {
                    totalRain += ((Number) value).doubleValue();
                }
            }
            count++;
        }

        return new double[]{ totalTemp / count, totalRain };
    }
}
