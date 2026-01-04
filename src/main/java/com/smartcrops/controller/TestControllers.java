package com.smartcrops.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestControllers {

    @GetMapping("/api/test")
    public String test() {
        return "JWT working";
    }
}
