package com.example.mall.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class OrderCreateDTO {
    @NotBlank(message = "请求幂等号不能为空")
    @Size(min = 8, max = 64, message = "请求幂等号长度需为 8-64 位")
    @Pattern(regexp = "^[A-Za-z0-9_-]+$", message = "请求幂等号只能包含字母、数字、下划线和短横线")
    private String requestId;

    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    @NotEmpty(message = "请选择要结算的购物车项")
    private List<Long> cartItemIds;
}
