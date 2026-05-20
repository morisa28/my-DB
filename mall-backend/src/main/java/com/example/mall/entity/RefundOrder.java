package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refund_order")
public class RefundOrder {
    @TableId
    private Long id;
    private String refundNo;
    private Long orderId;
    private String orderNo;
    private Long userId;
    private String paymentNo;
    private Integer applyType;
    private BigDecimal refundAmount;
    private String reason;
    private Integer status;
    private String refundChannel;
    private String thirdPartyRefundNo;
    private Integer stockRestoreRequired;
    private Integer stockRestored;
    private String adminRemark;
    private LocalDateTime applyTime;
    private LocalDateTime auditTime;
    private LocalDateTime refundTime;
    private LocalDateTime finishTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
