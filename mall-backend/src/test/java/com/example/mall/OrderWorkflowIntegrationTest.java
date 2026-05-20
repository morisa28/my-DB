package com.example.mall;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

class OrderWorkflowIntegrationTest extends MallIntegrationTestBase {

    @Test
    void orderStateFlowWritesAuditLogsAndRejectsStaleActions() {
        String userToken = login("user", "user123456");
        String adminToken = login("admin", "admin123456");

        ResponseEntity<JsonNode> orderResponse = postJson("/api/orders", Map.of(
                "addressId", 1,
                "cartItemIds", new Long[]{1L}
        ), userToken);
        assertOk(orderResponse);
        long orderId = orderResponse.getBody().path("data").path("orderId").asLong();

        assertOk(putJson("/api/orders/" + orderId + "/payment-note", Map.of("paymentNote", "集成测试付款备注"), userToken));
        assertOk(postJson("/api/admin/orders/" + orderId + "/confirm-payment", Map.of("adminRemark", "集成测试已收款"), adminToken));

        ResponseEntity<JsonNode> latePaymentNote = putJson("/api/orders/" + orderId + "/payment-note", Map.of("paymentNote", "迟到备注"), userToken);
        assertThat(latePaymentNote.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertOk(putJson("/api/admin/orders/" + orderId + "/ship", Map.of("shippingNo", "IT202605200001"), adminToken));
        assertOk(putJson("/api/orders/" + orderId + "/confirm-receipt", null, userToken));

        ResponseEntity<JsonNode> duplicateReceipt = putJson("/api/orders/" + orderId + "/confirm-receipt", null, userToken);
        assertThat(duplicateReceipt.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<JsonNode> detail = getJson("/api/orders/" + orderId, userToken);
        assertOk(detail);
        assertThat(detail.getBody().path("data").path("status").asInt()).isEqualTo(3);

        ResponseEntity<JsonNode> logs = getJson("/api/admin/orders/" + orderId + "/logs", adminToken);
        assertOk(logs);
        Set<String> actions = StreamSupport.stream(logs.getBody().path("data").spliterator(), false)
                .map(log -> log.path("action").asText())
                .collect(Collectors.toSet());
        assertThat(actions).containsExactlyInAnyOrder(
                "CREATE_ORDER",
                "SUBMIT_PAYMENT_NOTE",
                "CONFIRM_PAYMENT",
                "SHIP_ORDER",
                "CONFIRM_RECEIPT"
        );
    }

    @Test
    void concurrentCheckoutOfSameCartItemOnlySucceedsOnce() throws Exception {
        String userToken = login("user", "user123456");
        CountDownLatch start = new CountDownLatch(1);
        var executor = Executors.newFixedThreadPool(2);
        try {
            var task = (java.util.concurrent.Callable<ResponseEntity<JsonNode>>) () -> {
                start.await();
                return postJson("/api/orders", Map.of(
                        "addressId", 1,
                        "cartItemIds", new Long[]{1L}
                ), userToken);
            };
            var first = executor.submit(task);
            var second = executor.submit(task);
            start.countDown();

            var responses = java.util.List.of(first.get(), second.get());
            long successCount = responses.stream().filter(response -> response.getStatusCode().value() == 200).count();
            long rejectedCount = responses.stream().filter(response -> response.getStatusCode().value() == 400).count();

            assertThat(successCount).isEqualTo(1);
            assertThat(rejectedCount).isEqualTo(1);
        } finally {
            executor.shutdownNow();
        }
    }
}
