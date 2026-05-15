package com.example.mall.controller;

import com.example.mall.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {
    @GetMapping
    public Result<Map<String, Object>> health() {
        return Result.ok(Map.of(
                "status", "UP",
                "time", LocalDateTime.now()
        ));
    }
}

