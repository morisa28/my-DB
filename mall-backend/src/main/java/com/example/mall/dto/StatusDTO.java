package com.example.mall.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusDTO {
    @NotNull(message = "状态不能为空")
    private Integer status;
}

