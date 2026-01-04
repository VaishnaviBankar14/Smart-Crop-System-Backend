package com.smartcrops.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.smartcrops.jwt.JwtUtil;
import com.smartcrops.model.Crop;
import com.smartcrops.model.RecommendationHistory;
import com.smartcrops.repository.CropRepository;
import com.smartcrops.repository.RecommendationHistoryRepository;
import com.smartcrops.service.GeoCodingService;
import com.smartcrops.service.WeatherService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/crops")
public class CropController {

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private GeoCodingService geoCodingService;

    @Autowired
    private RecommendationHistoryRepository historyRepository;

    @Autowired
    private CropRepository cropRepository;

    // ===============================
    // 1️⃣ ADD CROP (Admin API)
    // ===============================
    @PostMapping("/add")
    public Crop addCrop(@RequestBody Crop crop) {
        return cropRepository.save(crop);
    }

    // ===============================
    // 2️⃣ GET ALL CROPS
    // ===============================
    @GetMapping("/all")
    public List<Crop> getAllCrops() {
        return cropRepository.findAll();
    }

    // ===============================
    // 3️⃣ MANUAL CROP RECOMMENDATION
    // ===============================
    @GetMapping("/recommend")
    public List<Crop> recommendCrop(
            @RequestParam double soilPh,
            @RequestParam String season,
            @RequestParam double rainfall) {

        List<Crop> crops = cropRepository.findBySeason(season);

        return crops.stream()
                .filter(c -> soilPh >= c.getMinPh() && soilPh <= c.getMaxPh())
                .filter(c -> rainfall >= c.getMinRainfall()
                          && rainfall <= c.getMaxRainfall())
                .toList();
    }

    // ===============================
    // 4️⃣ YIELD PREDICTION API
    // ===============================
    @GetMapping("/predict-yield")
    public double predictYield(
            @RequestParam Long cropId,
            @RequestParam double soilPh,
            @RequestParam double rainfall) {

        Crop crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new RuntimeException("Crop not found"));

        double phFactor;
        double rainFactor;

        if (soilPh >= crop.getMinPh() && soilPh <= crop.getMaxPh()) {
            phFactor = 1.0;
        } else if (Math.abs(soilPh - crop.getMinPh()) <= 1 ||
                   Math.abs(soilPh - crop.getMaxPh()) <= 1) {
            phFactor = 0.8;
        } else {
            phFactor = 0.5;
        }

        if (rainfall >= crop.getMinRainfall() && rainfall <= crop.getMaxRainfall()) {
            rainFactor = 1.0;
        } else if (Math.abs(rainfall - crop.getMinRainfall()) <= 20 ||
                   Math.abs(rainfall - crop.getMaxRainfall()) <= 20) {
            rainFactor = 0.85;
        } else {
            rainFactor = 0.6;
        }

        return crop.getYieldPerAcre() * phFactor * rainFactor;
    }

    // ===============================
    // 5️⃣ AUTO CROP RECOMMENDATION
    // ===============================
    @GetMapping("/auto-recommend")
    public List<Crop> autoRecommendCrop(
            @RequestParam String city,
            @RequestParam double soilPh) {

        double[] latLon = geoCodingService.getLatLon(city);

        Map<String, Object> weather =
                weatherService.getWeatherByLatLon(latLon[0], latLon[1]);

        double temperature = weatherService.getTemperature(weather);
        double rainfall = weatherService.getRainfall(weather);
        String season = weatherService.detectSeason(temperature, rainfall);

        List<Crop> crops = cropRepository.findBySeason(season);

        return crops.stream()
                .filter(c -> soilPh >= c.getMinPh() && soilPh <= c.getMaxPh())
                .toList();
    }

    // ===============================
    // 6️⃣ WEATHER + YIELD + HISTORY
    // ===============================
    @GetMapping("/auto-recommend-yield")
    public ResponseEntity<List<com.smartcrops.dto.CropYieldResponse>> autoRecommendWithYield(
            @RequestParam String city,
            @RequestParam double soilPh,
            HttpServletRequest request) {

        // ✅ READ EMAIL DIRECTLY FROM JWT (FINAL FIX)
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        String email = JwtUtil.extractUsername(token);

        double[] latLon = geoCodingService.getLatLon(city);

        Map<String, Object> weather =
                weatherService.getWeatherByLatLon(latLon[0], latLon[1]);

        double rainfall = weatherService.getRainfall(weather);
        String season = weatherService.detectSeason(
                weatherService.getTemperature(weather), rainfall);

        List<Crop> crops = cropRepository.findBySeason(season);

        List<com.smartcrops.dto.CropYieldResponse> result =
            crops.stream()
                .filter(c -> soilPh >= c.getMinPh() && soilPh <= c.getMaxPh())
                .map(c -> {

                    double rainFactor = 1.0;
                    if (rainfall < c.getMinRainfall()) rainFactor = 0.7;
                    if (rainfall > c.getMaxRainfall()) rainFactor = 0.8;

                    double expectedYield = c.getYieldPerAcre() * rainFactor;

                    RecommendationHistory history = new RecommendationHistory();
                    history.setUserEmail(email);
                    history.setCropName(c.getName());
                    history.setSeason(c.getSeason());
                    history.setExpectedYield(expectedYield);
                    historyRepository.save(history);

                    return new com.smartcrops.dto.CropYieldResponse(
                            c.getName(),
                            c.getSeason(),
                            expectedYield
                    );
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    // ===============================
    // 7️⃣ ADMIN – ALL HISTORY
    // ===============================
    @GetMapping("/history/all")
    public ResponseEntity<?> allHistory(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        String role = JwtUtil.extractRole(token);

        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }

        return ResponseEntity.ok(historyRepository.findAll());
    }

    // ===============================
    // 8️⃣ FARMER – MY HISTORY
    // ===============================
    @GetMapping("/history/my")
    public ResponseEntity<List<RecommendationHistory>> myHistory(
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        String email = JwtUtil.extractUsername(token);

        return ResponseEntity.ok(
                historyRepository.findByUserEmail(email)
        );
    }
}
