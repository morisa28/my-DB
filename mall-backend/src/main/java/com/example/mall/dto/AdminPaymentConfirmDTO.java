package com.example.mall.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminPaymentConfirmDTO {
    @Size(max = 255, message = "管理员备注不能超过255个字符")
    private String adminRemark;
}
