package com.example.mall.controller;

import com.example.mall.common.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HealthController {
    private final DataSource dataSource;

    @GetMapping("/health")
    public Result<Map<String, Object>> health() {
        return Result.ok(Map.of(
                "status", "UP",
                "time", LocalDateTime.now()
        ));
    }

    @GetMapping("/ready")
    public ResponseEntity<Result<Map<String, Object>>> ready() {
        if (!databaseReady()) {
            return ResponseEntity.status(503).body(Result.error(503, "服务依赖暂不可用"));
        }
        return ResponseEntity.ok(Result.ok(Map.of(
                "status", "UP",
                "database", "UP",
                "time", LocalDateTime.now()
        )));
    }

    private boolean databaseReady() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {
            return resultSet.next() && resultSet.getInt(1) == 1;
        } catch (Exception e) {
            return false;
        }
    }
}
