package com.smartcrops.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "chatbot_messages")
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long userId;

  @Column(columnDefinition = "TEXT")
  private String userMessage;

  @Column(columnDefinition = "TEXT")
  private String botResponse;

  private String language;

  private LocalDateTime timestamp = LocalDateTime.now();

  public Long getId() {
	    return id;
	}

	public void setId(Long id) {
	    this.id = id;
	}

	public Long getUserId() {
	    return userId;
	}

	public void setUserId(Long userId) {
	    this.userId = userId;
	}

	public String getUserMessage() {
	    return userMessage;
	}

	public void setUserMessage(String userMessage) {
	    this.userMessage = userMessage;
	}

	public String getBotResponse() {
	    return botResponse;
	}

	public void setBotResponse(String botResponse) {
	    this.botResponse = botResponse;
	}

	public String getLanguage() {
	    return language;
	}

	public void setLanguage(String language) {
	    this.language = language;
	}

	public LocalDateTime getTimestamp() {
	    return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
	    this.timestamp = timestamp;
	}

}
