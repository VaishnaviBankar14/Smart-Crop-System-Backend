package com.smartcrops.weather;

import java.util.Map;

public class WeatherResponse {

    public Map<String, Object> main;   // temp, humidity
    public Map<String, Object> rain;   // rainfall
    public Map<String, Object> wind;   // wind speed

}
