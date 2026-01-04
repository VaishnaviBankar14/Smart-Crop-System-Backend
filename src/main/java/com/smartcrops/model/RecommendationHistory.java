package com.smartcrops.model;

import jakarta.persistence.*;

@Entity
@Table(name = "recommendation_history")
public class RecommendationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;
    private String cropName;
    private String season;
    private double expectedYield;

    // getters & setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getCropName() { return cropName; }
    public void setCropName(String cropName) { this.cropName = cropName; }

    public String getSeason() { return season; }
    public void setSeason(String season) { this.season = season; }

    public double getExpectedYield() { return expectedYield; }
    public void setExpectedYield(double expectedYield) { this.expectedYield = expectedYield; }
}
