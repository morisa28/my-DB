package com.example.mall.dto;

import lombok.Data;

@Data
public class ProductQueryDTO {
    private Long page = 1L;
    private Long size = 8L;
    private Long categoryId;
    private String keyword;
    private Integer status;
}

