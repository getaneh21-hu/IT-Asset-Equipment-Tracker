
package com.It_Asset.it_asset_equipment_tracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Welcome to the IT Asset & Equipment Tracker API!";
    }

    @GetMapping("/api/health")
    public String health() {
        return "IT Asset Tracker is running!";
    }
}
