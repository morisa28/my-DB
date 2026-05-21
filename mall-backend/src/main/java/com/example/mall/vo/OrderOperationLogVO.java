package com.example.mall.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderOperationLogVO {
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
    private LocalDateTime createTime;
}
