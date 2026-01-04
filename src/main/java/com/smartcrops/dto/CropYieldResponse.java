package com.smartcrops.dto;

public class CropYieldResponse {

    private String cropName;
    private String season;
    private double expectedYield;

    public CropYieldResponse(String cropName, String season, double expectedYield) {
        this.cropName = cropName;
        this.season = season;
        this.expectedYield = expectedYield;
    }

    public String getCropName() {
        return cropName;
    }

    public String getSeason() {
        return season;
    }

    public double getExpectedYield() {
        return expectedYield;
    }
}
