package com.example.mall.controller;

import com.example.mall.common.PageResult;
import com.example.mall.common.Result;
import com.example.mall.dto.StatusDTO;
import com.example.mall.security.RequireAdmin;
import com.example.mall.service.UserService;
import com.example.mall.vo.UserOrderSummaryVO;
import com.example.mall.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequireAdmin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {
    private final UserService userService;

    @GetMapping
    public Result<PageResult<UserVO>> list(@RequestParam(defaultValue = "1") Long page,
                                           @RequestParam(defaultValue = "10") Long size,
                                           @RequestParam(required = false) String keyword) {
        return Result.ok(userService.pageUsers(page, size, keyword));
    }

    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        userService.updateUserStatus(id, dto.getStatus());
        return Result.ok();
    }

    @GetMapping("/{id}/order-summary")
    public Result<UserOrderSummaryVO> orderSummary(@PathVariable Long id) {
        return Result.ok(userService.getUserOrderSummary(id));
    }
}
