package com.example.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRankVO {
    private Long productId;
    private String productName;
    private Integer sales;
    private BigDecimal salesAmount;
}

