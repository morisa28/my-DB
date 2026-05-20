package com.example.mall.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ProductQueryDTO {
    @Min(value = 1, message = "页码不能小于 1")
    private Long page = 1L;

    @Min(value = 1, message = "每页数量不能小于 1")
    @Max(value = 100, message = "每页数量不能超过 100")
    private Long size = 8L;

    private Long categoryId;
    private String keyword;

    @Min(value = 0, message = "商品状态只能是 0 或 1")
    @Max(value = 1, message = "商品状态只能是 0 或 1")
    private Integer status;

    private Boolean lowStock;
}
