package com.example.mall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserOrderSummaryVO {
    private Long userId;
    private Long totalOrders;
    private BigDecimal paidAmount;
    private Long waitingPaymentOrders;
    private Long waitingShipmentOrders;
    private Long shippedOrders;
    private Long finishedOrders;
    private Long canceledOrders;
    private LocalDateTime lastOrderTime;
}
