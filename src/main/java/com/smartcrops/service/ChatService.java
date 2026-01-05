package com.smartcrops.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Map;

@Service
public class ChatService {

    private Map<String, Map<String, String>> cropRules;

    // Load JSON once when app starts
    @PostConstruct
    public void loadRules() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            InputStream is = new ClassPathResource(
                "crop_chatbot_rules_22.json"
            ).getInputStream();

            cropRules = mapper.readValue(is, Map.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getReply(String msg, String lang) {

        if (msg == null || msg.trim().isEmpty()) {
            return defaultMsg(lang);
        }

        msg = msg.toLowerCase();

        // 🔍 Check crop names in message
        for (String crop : cropRules.keySet()) {
            if (msg.contains(crop)) {
                return buildCropResponse(crop, lang);
            }
        }

        return defaultMsg(lang);
    }

    private String buildCropResponse(String crop, String lang) {

        Map<String, String> c = cropRules.get(crop);

        // 🌍 Marathi
        if ("mr".equalsIgnoreCase(lang)) {
            return "🌱 पीक: " + crop +
                   "\nहंगाम: " + c.get("season") +
                   "\nमाती: " + c.get("soil") +
                   "\npH: " + c.get("ph") +
                   "\nपाऊस: " + c.get("rainfall") +
                   "\nटीप: " + c.get("tips");
        }

        // 🌍 Hindi
        if ("hi".equalsIgnoreCase(lang)) {
            return "🌱 फसल: " + crop +
                   "\nमौसम: " + c.get("season") +
                   "\nमिट्टी: " + c.get("soil") +
                   "\npH: " + c.get("ph") +
                   "\nवर्षा: " + c.get("rainfall") +
                   "\nसलाह: " + c.get("tips");
        }

        // 🌍 English (default)
        return "🌱 Crop: " + crop +
               "\nSeason: " + c.get("season") +
               "\nSoil: " + c.get("soil") +
               "\npH Range: " + c.get("ph") +
               "\nRainfall: " + c.get("rainfall") +
               "\nTip: " + c.get("tips");
    }

    private String defaultMsg(String lang) {
        if ("mr".equalsIgnoreCase(lang)) {
            return "माफ करा, मला हा प्रश्न समजला नाही. कृपया पिकाबद्दल विचारा.";
        }
        if ("hi".equalsIgnoreCase(lang)) {
            return "माफ़ कीजिए, मैं आपका प्रश्न समझ नहीं पाया।";
        }
        return "Sorry, I could not understand your question. Please ask about crops.";
    }
}
