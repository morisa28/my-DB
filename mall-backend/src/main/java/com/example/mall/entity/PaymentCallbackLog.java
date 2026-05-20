package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("payment_callback_log")
public class PaymentCallbackLog {
    @TableId
    private Long id;
    private String paymentNo;
    private String channel;
    private String eventType;
    private String rawPayload;
    private Integer verifyResult;
    private Integer processResult;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
