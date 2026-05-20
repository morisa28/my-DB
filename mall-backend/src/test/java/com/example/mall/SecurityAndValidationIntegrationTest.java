package com.example.mall;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityAndValidationIntegrationTest extends MallIntegrationTestBase {

    @Test
    void protectsAdminEndpointsAndValidatesQueryParameters() {
        String userToken = login("user", "user123456");
        String adminToken = login("admin", "admin123456");

        ResponseEntity<JsonNode> anonymousCart = getJson("/api/cart", null);
        assertThat(anonymousCart.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

        ResponseEntity<JsonNode> forbiddenStatistics = getJson("/api/admin/statistics", userToken);
        assertThat(forbiddenStatistics.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<JsonNode> invalidQuery = getJson("/api/admin/orders?page=0&size=500&status=9", adminToken);
        assertThat(invalidQuery.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertOk(putJson("/api/admin/users/3/status", java.util.Map.of("status", 0), adminToken));
        ResponseEntity<JsonNode> operationLogs = getJson("/api/admin/operation-logs?module=USER&action=UPDATE_USER_STATUS", adminToken);
        assertOk(operationLogs);
        JsonNode firstLog = operationLogs.getBody().path("data").path("records").path(0);
        assertThat(firstLog.path("module").asText()).isEqualTo("USER");
        assertThat(firstLog.path("action").asText()).isEqualTo("UPDATE_USER_STATUS");
        assertThat(firstLog.path("targetId").asLong()).isEqualTo(3L);
    }

    @Test
    void rejectsNonAdminUploadsAndInvalidImageContent() {
        String userToken = login("user", "user123456");
        String adminToken = login("admin", "admin123456");

        ResponseEntity<JsonNode> forbiddenUpload = upload("fake.png", "not a png".getBytes(StandardCharsets.UTF_8), userToken);
        assertThat(forbiddenUpload.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        ResponseEntity<JsonNode> invalidImage = upload("fake.png", "not a png".getBytes(StandardCharsets.UTF_8), adminToken);
        assertThat(invalidImage.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    private ResponseEntity<JsonNode> upload(String filename, byte[] bytes, String token) {
        HttpHeaders fileHeaders = new HttpHeaders();
        fileHeaders.setContentType(MediaType.IMAGE_PNG);
        HttpEntity<?> filePart = new HttpEntity<>(namedResource(filename, bytes), fileHeaders);

        var body = new LinkedMultiValueMap<String, Object>();
        body.add("file", filePart);

        HttpHeaders headers = headers(token);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return restTemplate.exchange(
                "/api/admin/upload/product-image",
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                JsonNode.class
        );
    }
}
