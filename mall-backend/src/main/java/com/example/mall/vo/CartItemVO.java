package com.example.mall.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CartItemVO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private String productImage;
    private Integer productStatus;
    private Integer stock;
    private Integer quantity;
    private BigDecimal totalPrice;
}

