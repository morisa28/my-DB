package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.security.RequireAdmin;
import com.example.mall.service.OrderService;
import com.example.mall.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequireAdmin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {
    private final OrderService orderService;

    @GetMapping
    public Result<StatisticsVO> statistics() {
        return Result.ok(orderService.statistics());
    }
}

