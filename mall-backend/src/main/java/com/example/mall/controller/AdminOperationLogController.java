package com.example.mall.controller;

import com.example.mall.common.PageResult;
import com.example.mall.common.Result;
import com.example.mall.dto.AdminOperationLogQueryDTO;
import com.example.mall.security.RequireAdmin;
import com.example.mall.service.AdminOperationLogService;
import com.example.mall.vo.AdminOperationLogVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequireAdmin
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/operation-logs")
public class AdminOperationLogController {
    private final AdminOperationLogService adminOperationLogService;

    @GetMapping
    public Result<PageResult<AdminOperationLogVO>> page(@Valid @ModelAttribute AdminOperationLogQueryDTO query) {
        return Result.ok(adminOperationLogService.pageLogs(query));
    }
}
