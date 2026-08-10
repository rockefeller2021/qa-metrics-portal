package com.qametrics.portal.infrastructure.adapters.in.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller de Health Check para Render / Cloud Probes.
 */
@RestController
@RequestMapping
public class HealthController {

    @GetMapping({"/", "/health", "/ping"})
    public ResponseEntity<Map<String, Object>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "qa-metrics-portal-backend",
                "version", "2.0.0"
        ));
    }
}
