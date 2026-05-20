package com.example.mall;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.StreamUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class MallIntegrationTestBase {
    private static final Path UPLOAD_DIR = Path.of("target/test-uploads").toAbsolutePath();

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    private DataSource dataSource;

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.datasource.url", () -> envOrDefault(
                "TEST_DB_URL",
                "jdbc:mysql://localhost:3317/mall_db?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true"
        ));
        registry.add("spring.datasource.username", () -> envOrDefault("TEST_DB_USERNAME", "root"));
        registry.add("spring.datasource.password", () -> envOrDefault("TEST_DB_PASSWORD", "mall_demo_root_2026"));
        registry.add("mall.jwt.secret", () -> "mall-integration-test-secret-at-least-32-bytes-2026");
        registry.add("mall.cors.allowed-origins", () -> "http://localhost:5173");
        registry.add("mall.upload.dir", () -> UPLOAD_DIR.toString());
        registry.add("mall.upload.base-url", () -> "/uploads");
        registry.add("mybatis-plus.configuration.log-impl", () -> "org.apache.ibatis.logging.nologging.NoLoggingImpl");
    }

    @BeforeEach
    @AfterEach
    void resetDatabase() throws Exception {
        executeStatement("SET FOREIGN_KEY_CHECKS=0");
        for (String table : new String[]{
                "order_operation_log", "order_item", "order_info", "cart_item",
                "address", "product", "category", "`user`"
        }) {
            executeStatement("DROP TABLE IF EXISTS " + table);
        }
        executeStatement("SET FOREIGN_KEY_CHECKS=1");
        executeScript("sql/schema.sql");
        executeScript("sql/data.sql");
    }

    protected String login(String username, String password) {
        ResponseEntity<JsonNode> response = postJson("/api/user/login", Map.of(
                "username", username,
                "password", password
        ), null);
        assertOk(response);
        return response.getBody().path("data").path("token").asText();
    }

    protected ResponseEntity<JsonNode> getJson(String path, String token) {
        return exchange(path, HttpMethod.GET, null, token);
    }

    protected ResponseEntity<JsonNode> postJson(String path, Object body, String token) {
        return exchange(path, HttpMethod.POST, body, token);
    }

    protected ResponseEntity<JsonNode> putJson(String path, Object body, String token) {
        return exchange(path, HttpMethod.PUT, body, token);
    }

    protected ResponseEntity<JsonNode> exchange(String path, HttpMethod method, Object body, String token) {
        HttpHeaders headers = headers(token);
        if (body != null) {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
        return restTemplate.exchange(path, method, new HttpEntity<>(body, headers), JsonNode.class);
    }

    protected HttpHeaders headers(String token) {
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.setBearerAuth(token);
        }
        return headers;
    }

    protected void assertOk(ResponseEntity<JsonNode> response) {
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().path("code").asInt()).isEqualTo(200);
    }

    protected ByteArrayResource namedResource(String filename, byte[] bytes) {
        return new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }

    private void executeStatement(String sql) {
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try (var statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to execute SQL statement: " + sql, e);
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private void executeScript(String classpathResource) throws Exception {
        String sql = readFilteredScript(classpathResource);
        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            org.springframework.jdbc.datasource.init.ScriptUtils.executeSqlScript(
                    connection,
                    new ByteArrayResource(sql.getBytes(StandardCharsets.UTF_8))
            );
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }

    private String readFilteredScript(String classpathResource) throws IOException {
        ClassPathResource resource = new ClassPathResource(classpathResource);
        String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        return sql.lines()
                .filter(line -> {
                    String trimmed = line.trim().toUpperCase();
                    return !trimmed.startsWith("DROP DATABASE")
                            && !trimmed.startsWith("CREATE DATABASE")
                            && !trimmed.startsWith("USE ");
                })
                .collect(Collectors.joining(System.lineSeparator()));
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
