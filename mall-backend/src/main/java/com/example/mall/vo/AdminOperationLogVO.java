package com.example.mall.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AdminOperationLogVO {
    private Long id;
    private Long operatorId;
    private String operatorUsername;
    private String module;
    private String action;
    private Long targetId;
    private String targetName;
    private String remark;
    private LocalDateTime createTime;
}
