package com.example.mall.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotEmpty(message = "请选择要结算的购物车项")
    private List<Long> cartItemIds;
}

