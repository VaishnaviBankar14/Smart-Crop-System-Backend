package com.smartcrops.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    // ⭐ HELPER: SELECT ONE BEST CROP (UNCHANGED)
    // ===============================
    private Crop selectBestCrop(
            List<Crop> crops,
            double soilPh,
            double rainfall) {

        Crop best = null;
        double bestScore = -1;

        for (Crop c : crops) {

            if (rainfall < c.getMinRainfall() - 50 ||
                rainfall > c.getMaxRainfall() + 50) {
                continue;
            }

            double phMid = (c.getMinPh() + c.getMaxPh()) / 2;
            double phScore = 1 / (1 + Math.abs(soilPh - phMid));

            double rainMid = (c.getMinRainfall() + c.getMaxRainfall()) / 2;
            double rainScore = 1 / (1 + Math.abs(rainfall - rainMid));

            double yieldScore = c.getYieldPerAcre() / 100;

            double totalScore = phScore + rainScore + yieldScore;

            if (totalScore > bestScore) {
                bestScore = totalScore;
                best = c;
            }
        }
        return best;
    }

    // ===============================
    // 1️⃣ ADD CROP (Admin)
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
    // 3️⃣ MANUAL RECOMMENDATION (UNCHANGED)
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
    // 4️⃣ YIELD PREDICTION (UNCHANGED)
    // ===============================
    @GetMapping("/predict-yield")
    public double predictYield(
            @RequestParam Long cropId,
            @RequestParam double soilPh,
            @RequestParam double rainfall) {

        Crop crop = cropRepository.findById(cropId)
                .orElseThrow(() -> new RuntimeException("Crop not found"));

        double phFactor =
                (soilPh >= crop.getMinPh() && soilPh <= crop.getMaxPh())
                        ? 1.0 : 0.7;

        double rainFactor =
                (rainfall >= crop.getMinRainfall()
                 && rainfall <= crop.getMaxRainfall())
                        ? 1.0 : 0.8;

        return crop.getYieldPerAcre() * phFactor * rainFactor;
    }

    // ===============================
    // 5️⃣ AUTO RECOMMEND (ONE CROP)
    // ===============================
    @GetMapping("/auto-recommend")
    public Crop autoRecommendCrop(
            @RequestParam String city,
            @RequestParam double soilPh) {

        // ✅ ONLY ADDITION: pH LIMIT
        if (soilPh < 0 || soilPh > 9) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Soil pH must be between 0 and 9"
            );
        }

        double[] latLon = geoCodingService.getLatLon(city);

        Map<String, Object> weather =
                weatherService.getWeatherByLatLon(latLon[0], latLon[1]);

        double rainfall = weatherService.getRainfall(weather);
        String season = weatherService.detectSeason(
                weatherService.getTemperature(weather), rainfall);

        List<Crop> crops = cropRepository.findBySeason(season);

        return selectBestCrop(crops, soilPh, rainfall);
    }

    // ===============================
    // 6️⃣ AUTO RECOMMEND + YIELD + HISTORY
    // ===============================
    @GetMapping("/auto-recommend-yield")
    public ResponseEntity<com.smartcrops.dto.CropYieldResponse>
    autoRecommendWithYield(
            @RequestParam String city,
            @RequestParam double soilPh,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // ✅ ONLY ADDITION: pH LIMIT
        if (soilPh < 0 || soilPh > 9) {
            return ResponseEntity.badRequest().build();
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

        Crop best = selectBestCrop(crops, soilPh, rainfall);
        if (best == null) return ResponseEntity.ok(null);

        double rainFactor = 1.0;
        if (rainfall < best.getMinRainfall()) rainFactor = 0.7;
        if (rainfall > best.getMaxRainfall()) rainFactor = 0.8;

        double expectedYield = best.getYieldPerAcre() * rainFactor;

        RecommendationHistory history = new RecommendationHistory();
        history.setUserEmail(email);
        history.setCropName(best.getName());
        history.setSeason(best.getSeason());
        history.setExpectedYield(expectedYield);
        historyRepository.save(history);

        return ResponseEntity.ok(
                new com.smartcrops.dto.CropYieldResponse(
                        best.getName(),
                        best.getSeason(),
                        expectedYield
                )
        );
    }

    // ===============================
    // 7️⃣ ADMIN – ALL HISTORY (UNCHANGED)
    // ===============================
    @GetMapping("/history/all")
    public ResponseEntity<?> allHistory(HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String role = JwtUtil.extractRole(authHeader.substring(7));
        if (!"ADMIN".equals(role)) {
            return ResponseEntity.status(403).body("Access denied: Admins only");
        }

        return ResponseEntity.ok(historyRepository.findAll());
    }

    // ===============================
    // 8️⃣ FARMER – MY HISTORY (UNCHANGED)
    // ===============================
    @GetMapping("/history/my")
    public ResponseEntity<List<RecommendationHistory>> myHistory(
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = JwtUtil.extractUsername(authHeader.substring(7));

        return ResponseEntity.ok(
                historyRepository.findByUserEmail(email)
        );
    }
}



//
////package com.smartcrops.controller;
////
////import java.util.List;
////import java.util.Map;
////
////import org.springframework.beans.factory.annotation.Autowired;
////import org.springframework.http.HttpStatus;
////import org.springframework.http.ResponseEntity;
////import org.springframework.web.bind.annotation.*;
////
////import com.smartcrops.jwt.JwtUtil;
////import com.smartcrops.model.Crop;
////import com.smartcrops.model.RecommendationHistory;
////import com.smartcrops.repository.CropRepository;
////import com.smartcrops.repository.RecommendationHistoryRepository;
////import com.smartcrops.service.GeoCodingService;
////import com.smartcrops.service.WeatherService;
////
////import jakarta.servlet.http.HttpServletRequest;
////
////@RestController
////@RequestMapping("/api/crops")
////public class CropController {
////
////    @Autowired
////    private WeatherService weatherService;
////
////    @Autowired
////    private GeoCodingService geoCodingService;
////
////    @Autowired
////    private RecommendationHistoryRepository historyRepository;
////
////    @Autowired
////    private CropRepository cropRepository;
////
////    // ===============================
////    // 1️⃣ ADD CROP (Admin API)
////    // ===============================
////    @PostMapping("/add")
////    public Crop addCrop(@RequestBody Crop crop) {
////        return cropRepository.save(crop);
////    }
////
////    // ===============================
////    // 2️⃣ GET ALL CROPS
////    // ===============================
////    @GetMapping("/all")
////    public List<Crop> getAllCrops() {
////        return cropRepository.findAll();
////    }
////
////    // ===============================
////    // 3️⃣ MANUAL CROP RECOMMENDATION
////    // ===============================
////    @GetMapping("/recommend")
////    public List<Crop> recommendCrop(
////            @RequestParam double soilPh,
////            @RequestParam String season,
////            @RequestParam double rainfall) {
////
////        List<Crop> crops = cropRepository.findBySeason(season);
////
////        return crops.stream()
////                .filter(c -> soilPh >= c.getMinPh() && soilPh <= c.getMaxPh())
////                .filter(c -> rainfall >= c.getMinRainfall()
////                          && rainfall <= c.getMaxRainfall())
////                .toList();
////    }
////
////    // ===============================
////    // 4️⃣ YIELD PREDICTION API
////    // ===============================
////    @GetMapping("/predict-yield")
////    public double predictYield(
////            @RequestParam Long cropId,
////            @RequestParam double soilPh,
////            @RequestParam double rainfall) {
////
////        Crop crop = cropRepository.findById(cropId)
////                .orElseThrow(() -> new RuntimeException("Crop not found"));
////
////        double phFactor;
////        double rainFactor;
////
////        if (soilPh >= crop.getMinPh() && soilPh <= crop.getMaxPh()) {
////            phFactor = 1.0;
////        } else if (Math.abs(soilPh - crop.getMinPh()) <= 1 ||
////                   Math.abs(soilPh - crop.getMaxPh()) <= 1) {
////            phFactor = 0.8;
////        } else {
////            phFactor = 0.5;
////        }
////
////        if (rainfall >= crop.getMinRainfall() && rainfall <= crop.getMaxRainfall()) {
////            rainFactor = 1.0;
////        } else if (Math.abs(rainfall - crop.getMinRainfall()) <= 20 ||
////                   Math.abs(rainfall - crop.getMaxRainfall()) <= 20) {
////            rainFactor = 0.85;
////        } else {
////            rainFactor = 0.6;
////        }
////
////        return crop.getYieldPerAcre() * phFactor * rainFactor;
////    }
////
////    // ===============================
////    // 5️⃣ AUTO CROP RECOMMENDATION
////    // ===============================
////    @GetMapping("/auto-recommend")
////    public List<Crop> autoRecommendCrop(
////            @RequestParam String city,
////            @RequestParam double soilPh) {
////
////        double[] latLon = geoCodingService.getLatLon(city);
////
////        Map<String, Object> weather =
////                weatherService.getWeatherByLatLon(latLon[0], latLon[1]);
////
////        double temperature = weatherService.getTemperature(weather);
////        double rainfall = weatherService.getRainfall(weather);
////        String season = weatherService.detectSeason(temperature, rainfall);
////
////        List<Crop> crops = cropRepository.findBySeason(season);
////
////        return crops.stream()
////                .filter(c -> soilPh >= c.getMinPh() && soilPh <= c.getMaxPh())
////                .toList();
////    }
////
////    // ===============================
////    // 6️⃣ WEATHER + YIELD + HISTORY
////    // ===============================
////    @GetMapping("/auto-recommend-yield")
////    public ResponseEntity<List<com.smartcrops.dto.CropYieldResponse>> autoRecommendWithYield(
////            @RequestParam String city,
////            @RequestParam double soilPh,
////            HttpServletRequest request) {
////
////        // ✅ READ EMAIL DIRECTLY FROM JWT (FINAL FIX)
////        String authHeader = request.getHeader("Authorization");
////        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
////        }
////
////        String token = authHeader.substring(7);
////        String email = JwtUtil.extractUsername(token);
////
////        double[] latLon = geoCodingService.getLatLon(city);
////
////        Map<String, Object> weather =
////                weatherService.getWeatherByLatLon(latLon[0], latLon[1]);
////
////        double rainfall = weatherService.getRainfall(weather);
////        String season = weatherService.detectSeason(
////                weatherService.getTemperature(weather), rainfall);
////
////        List<Crop> crops = cropRepository.findBySeason(season);
////
////        List<com.smartcrops.dto.CropYieldResponse> result =
////            crops.stream()
////                .filter(c -> soilPh >= c.getMinPh() && soilPh <= c.getMaxPh())
////                .map(c -> {
////
////                    double rainFactor = 1.0;
////                    if (rainfall < c.getMinRainfall()) rainFactor = 0.7;
////                    if (rainfall > c.getMaxRainfall()) rainFactor = 0.8;
////
////                    double expectedYield = c.getYieldPerAcre() * rainFactor;
////
////                    RecommendationHistory history = new RecommendationHistory();
////                    history.setUserEmail(email);
////                    history.setCropName(c.getName());
////                    history.setSeason(c.getSeason());
////                    history.setExpectedYield(expectedYield);
////                    historyRepository.save(history);
////
////                    return new com.smartcrops.dto.CropYieldResponse(
////                            c.getName(),
////                            c.getSeason(),
////                            expectedYield
////                    );
////                })
////                .toList();
////
////        return ResponseEntity.ok(result);
////    }
////
////    // ===============================
////    // 7️⃣ ADMIN – ALL HISTORY
////    // ===============================
////    @GetMapping("/history/all")
////    public ResponseEntity<?> allHistory(HttpServletRequest request) {
////
////        String authHeader = request.getHeader("Authorization");
////        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
////        }
////
////        String token = authHeader.substring(7);
////        String role = JwtUtil.extractRole(token);
////
////        if (!"ADMIN".equals(role)) {
////            return ResponseEntity.status(403).body("Access denied: Admins only");
////        }
////
////        return ResponseEntity.ok(historyRepository.findAll());
////    }
////
////    // ===============================
////    // 8️⃣ FARMER – MY HISTORY
////    // ===============================
////    @GetMapping("/history/my")
////    public ResponseEntity<List<RecommendationHistory>> myHistory(
////            HttpServletRequest request) {
////
////        String authHeader = request.getHeader("Authorization");
////        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
////            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
////        }
////
////        String token = authHeader.substring(7);
////        String email = JwtUtil.extractUsername(token);
////
////        return ResponseEntity.ok(
////                historyRepository.findByUserEmail(email)
////        );
////    }
////}
