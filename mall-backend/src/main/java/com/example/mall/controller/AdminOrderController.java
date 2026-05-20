package com.example.mall.controller;

import com.example.mall.common.PageResult;
import com.example.mall.common.Result;
import com.example.mall.dto.AdminPaymentConfirmDTO;
import com.example.mall.dto.OrderQueryDTO;
import com.example.mall.dto.ShipOrderDTO;
import com.example.mall.security.RequireAdmin;
import com.example.mall.service.OrderService;
import com.example.mall.vo.OrderDetailVO;
import com.example.mall.vo.OrderOperationLogVO;
import com.example.mall.vo.OrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequireAdmin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/orders")
public class AdminOrderController {
    private final OrderService orderService;

    @GetMapping
    public Result<PageResult<OrderVO>> list(@Valid OrderQueryDTO query) {
        return Result.ok(orderService.pageAdminOrders(query));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) {
        return Result.ok(orderService.getAdminOrderDetail(id));
    }

    @GetMapping("/{id}/logs")
    public Result<List<OrderOperationLogVO>> logs(@PathVariable Long id) {
        return Result.ok(orderService.listAdminOrderLogs(id));
    }

    @PostMapping("/{id}/confirm-payment")
    public Result<Void> confirmPayment(@PathVariable Long id, @Valid @RequestBody(required = false) AdminPaymentConfirmDTO dto) {
        orderService.confirmPayment(id, dto);
        return Result.ok();
    }

    @PutMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id, @Valid @RequestBody(required = false) ShipOrderDTO dto) {
        orderService.shipOrder(id, dto);
        return Result.ok();
    }
}
