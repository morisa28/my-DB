package com.example.mall.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("order_operation_log")
public class OrderOperationLog {
    @TableId
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long operatorId;
    private String operatorUsername;
    private Integer operatorRole;
    private String action;
    private Integer fromStatus;
    private Integer toStatus;
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
