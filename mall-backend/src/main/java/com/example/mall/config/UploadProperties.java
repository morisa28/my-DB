package com.example.mall.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "mall.upload")
public class UploadProperties {
    private String dir = "./uploads";
    private String baseUrl = "/uploads";
    private long maxSizeBytes = 2 * 1024 * 1024;
    private List<String> allowedExtensions = new ArrayList<>(List.of("jpg", "jpeg", "png", "webp"));
}
