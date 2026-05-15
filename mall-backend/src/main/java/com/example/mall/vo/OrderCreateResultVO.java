package com.example.mall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateResultVO {
    private Long orderId;
    private String orderNo;
    private BigDecimal totalAmount;
}

