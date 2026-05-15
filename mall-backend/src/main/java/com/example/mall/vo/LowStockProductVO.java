package com.example.mall.vo;

import lombok.Data;

@Data
public class LowStockProductVO {
    private Long productId;
    private String productName;
    private Integer stock;
}

