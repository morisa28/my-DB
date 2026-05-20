package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("refund_operation_log")
public class RefundOperationLog {
    @TableId
    private Long id;
    private Long refundId;
    private String refundNo;
    private Long operatorId;
    private String operatorRole;
    private Integer fromStatus;
    private Integer toStatus;
    private String action;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
