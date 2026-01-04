package com.smartcrops.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.smartcrops.service.GeoCodingService;
import com.smartcrops.service.WeatherService;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

	@Autowired
	private GeoCodingService geoCodingService;

    @Autowired
    private WeatherService weatherService;

    @GetMapping("/city")
    public Object getWeather(@RequestParam String city) {
    	double[] latLon = geoCodingService.getLatLon(city);
    	return weatherService.getWeatherByLatLon(latLon[0], latLon[1]);

    }
}
