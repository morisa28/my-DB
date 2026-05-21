package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.common.PageResult;
import com.example.mall.dto.AdminOperationLogQueryDTO;
import com.example.mall.entity.AdminOperationLog;
import com.example.mall.mapper.AdminOperationLogMapper;
import com.example.mall.security.LoginUser;
import com.example.mall.security.UserContext;
import com.example.mall.service.AdminOperationLogService;
import com.example.mall.vo.AdminOperationLogVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class AdminOperationLogServiceImpl extends ServiceImpl<AdminOperationLogMapper, AdminOperationLog> implements AdminOperationLogService {

    @Override
    public PageResult<AdminOperationLogVO> pageLogs(AdminOperationLogQueryDTO query) {
        LambdaQueryWrapper<AdminOperationLog> wrapper = new LambdaQueryWrapper<AdminOperationLog>()
                .eq(StringUtils.hasText(query.getModule()), AdminOperationLog::getModule, normalize(query.getModule()))
                .eq(StringUtils.hasText(query.getAction()), AdminOperationLog::getAction, normalize(query.getAction()))
                .orderByDesc(AdminOperationLog::getCreateTime)
                .orderByDesc(AdminOperationLog::getId);
        Page<AdminOperationLog> page = page(new Page<>(query.getPage(), query.getSize()), wrapper);
        List<AdminOperationLogVO> records = page.getRecords().stream()
                .map(this::toVO)
                .toList();
        return new PageResult<>(page.getTotal(), page.getCurrent(), page.getSize(), records);
    }

    @Override
    public void record(String module, String action, Long targetId, String targetName, String remark) {
        LoginUser operator = UserContext.get();
        AdminOperationLog log = new AdminOperationLog();
        log.setOperatorId(operator.getId());
        log.setOperatorUsername(operator.getUsername());
        log.setModule(normalize(module));
        log.setAction(normalize(action));
        log.setTargetId(targetId);
        log.setTargetName(normalize(targetName));
        log.setRemark(normalize(remark));
        save(log);
    }

    private AdminOperationLogVO toVO(AdminOperationLog log) {
        AdminOperationLogVO vo = new AdminOperationLogVO();
        BeanUtils.copyProperties(log, vo);
        return vo;
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
