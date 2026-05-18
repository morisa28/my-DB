package com.example.mall.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentNoteDTO {
    @Size(max = 255, message = "付款备注不能超过255个字符")
    private String paymentNote;
}
