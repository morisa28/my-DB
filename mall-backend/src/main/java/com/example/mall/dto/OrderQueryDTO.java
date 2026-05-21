package com.example.mall.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class OrderQueryDTO {
    @Min(value = 1, message = "页码不能小于 1")
    private Long page = 1L;

    @Min(value = 1, message = "每页数量不能小于 1")
    @Max(value = 100, message = "每页数量不能超过 100")
    private Long size = 10L;

    @Min(value = 0, message = "订单状态只能是 0-4")
    @Max(value = 4, message = "订单状态只能是 0-4")
    private Integer status;
}
