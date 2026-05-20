package com.example.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.common.PageResult;
import com.example.mall.dto.AdminOperationLogQueryDTO;
import com.example.mall.entity.AdminOperationLog;
import com.example.mall.vo.AdminOperationLogVO;

public interface AdminOperationLogService extends IService<AdminOperationLog> {
    PageResult<AdminOperationLogVO> pageLogs(AdminOperationLogQueryDTO query);

    void record(String module, String action, Long targetId, String targetName, String remark);
}
