package com.smartcrops.dto;

public class ChatRequest {

    private String message;
    private String language;

    // 🔹 Default constructor
    public ChatRequest() {
    }

    // 🔹 Parameterized constructor
    public ChatRequest(String message, String language) {
        this.message = message;
        this.language = language;
    }

    // 🔹 Getter for message
    public String getMessage() {
        return message;
    }

    // 🔹 Setter for message
    public void setMessage(String message) {
        this.message = message;
    }

    // 🔹 Getter for language
    public String getLanguage() {
        return language;
    }

    // 🔹 Setter for language
    public void setLanguage(String language) {
        this.language = language;
    }
}
