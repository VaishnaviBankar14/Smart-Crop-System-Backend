package com.smartcrops.dto;

public class CropYieldConfidenceResponse {

    private String cropName;
    private String season;
    private double expectedYield;
    private int confidence; // %

    public CropYieldConfidenceResponse(
            String cropName,
            String season,
            double expectedYield,
            int confidence) {

        this.cropName = cropName;
        this.season = season;
        this.expectedYield = expectedYield;
        this.confidence = confidence;
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

    public int getConfidence() {
        return confidence;
    }
}
