package com.smartcrops.service;

import org.springframework.stereotype.Service;

@Service
public class ChatService {

    /**
     * Generates chatbot reply based on message and language
     * @param msg user message
     * @param lang selected language (en, hi, mr)
     * @return chatbot response
     */
    public String getReply(String msg, String lang) {

        if (msg == null || msg.trim().isEmpty()) {
            return "Please ask a valid question.";
        }

        msg = msg.toLowerCase();

        // =========================
        // 🌍 HINDI RESPONSES
        // =========================
        if ("hi".equalsIgnoreCase(lang)) {

            if (msg.contains("rice") || msg.contains("चावल")) {
                return "चावल चिकनी मिट्टी में और अधिक पानी के साथ अच्छे से उगता है।";
            }

            if (msg.contains("kharif") || msg.contains("खरीफ")) {
                return "खरीफ फसलें मानसून के मौसम में उगाई जाती हैं।";
            }

            if (msg.contains("soil") || msg.contains("मिट्टी")) {
                return "अलग-अलग फसलों के लिए अलग-अलग मिट्टी उपयुक्त होती है।";
            }
        }

        // =========================
        // 🌍 MARATHI RESPONSES
        // =========================
        if ("mr".equalsIgnoreCase(lang)) {

            if (msg.contains("rice") || msg.contains("भात")) {
                return "भात चिकणमाती जमिनीत आणि जास्त पाण्यात चांगला उगवतो.";
            }

            if (msg.contains("kharif") || msg.contains("खरीप")) {
                return "खरीप पिके पावसाळ्यात घेतली जातात.";
            }

            if (msg.contains("soil") || msg.contains("माती")) {
                return "वेगवेगळ्या पिकांसाठी वेगवेगळी माती योग्य असते.";
            }
        }

        // =========================
        // 🌍 ENGLISH RESPONSES
        // =========================
        if (msg.contains("rice")) {
            return "Rice grows best in clayey soil with high water availability.";
        }

        if (msg.contains("kharif")) {
            return "Kharif crops are grown during the monsoon season.";
        }

        if (msg.contains("soil")) {
            return "Different crops require different types of soil for better yield.";
        }

        if (msg.contains("water")) {
            return "Water requirement varies depending on the crop type.";
        }

        // =========================
        // ❓ DEFAULT RESPONSE
        // =========================
        return "Sorry, I could not understand your question. Please try asking about crops, soil, or seasons.";
    }
}
