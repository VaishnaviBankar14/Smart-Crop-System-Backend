package com.smartcrops.model;

import jakarta.persistence.*;

@Entity
@Table(name = "crops")
public class Crop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private double minPh;
    private double maxPh;

    private String season;   // Kharif, Rabi, Zaid

    private double minRainfall;
    private double maxRainfall;

    private double yieldPerAcre;

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMinPh() {
        return minPh;
    }

    public void setMinPh(double minPh) {
        this.minPh = minPh;
    }

    public double getMaxPh() {
        return maxPh;
    }

    public void setMaxPh(double maxPh) {
        this.maxPh = maxPh;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public double getMinRainfall() {
        return minRainfall;
    }

    public void setMinRainfall(double minRainfall) {
        this.minRainfall = minRainfall;
    }

    public double getMaxRainfall() {
        return maxRainfall;
    }

    public void setMaxRainfall(double maxRainfall) {
        this.maxRainfall = maxRainfall;
    }

    public double getYieldPerAcre() {
        return yieldPerAcre;
    }

    public void setYieldPerAcre(double yieldPerAcre) {
        this.yieldPerAcre = yieldPerAcre;
    }
}
