package com.example.mall.dto;

import lombok.Data;

@Data
public class OrderQueryDTO {
    private Long page = 1L;
    private Long size = 10L;
    private Integer status;
}

