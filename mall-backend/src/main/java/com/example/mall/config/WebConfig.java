package com.example.mall.config;

import com.example.mall.security.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final UploadProperties uploadProperties;
    private final CorsProperties corsProperties;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/user/register",
                        "/api/user/login",
                        "/api/health",
                        "/api/ready",
                        "/api/categories",
                        "/api/products",
                        "/api/products/*",
                        "/api/products/search"
                );
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String baseUrl = uploadProperties.getBaseUrl().startsWith("/")
                ? uploadProperties.getBaseUrl()
                : "/" + uploadProperties.getBaseUrl();
        baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        Path uploadRoot = Path.of(uploadProperties.getDir()).toAbsolutePath().normalize();
        String resourceLocation = uploadRoot.toUri().toString();
        resourceLocation = resourceLocation.endsWith("/") ? resourceLocation : resourceLocation + "/";
        registry.addResourceHandler(baseUrl + "/**")
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allowedOrigins = corsProperties.getAllowedOrigins().stream()
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toArray(String[]::new);
        registry.addMapping("/api/**")
                .allowedOriginPatterns(allowedOrigins.length == 0 ? new String[]{"*"} : allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
