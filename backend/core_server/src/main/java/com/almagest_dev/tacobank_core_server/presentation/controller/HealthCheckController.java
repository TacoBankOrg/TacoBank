package com.almagest_dev.tacobank_core_server.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/taco/core/health")
public class HealthCheckController {

    @GetMapping
    public String healthCheck() {
        return "OK";
    }
}
