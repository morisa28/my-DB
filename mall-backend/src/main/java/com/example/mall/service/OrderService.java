package com.example.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.common.PageResult;
import com.example.mall.dto.AdminPaymentConfirmDTO;
import com.example.mall.dto.OrderCreateDTO;
import com.example.mall.dto.OrderQueryDTO;
import com.example.mall.dto.PaymentNoteDTO;
import com.example.mall.dto.ShipOrderDTO;
import com.example.mall.entity.OrderInfo;
import com.example.mall.vo.OrderCreateResultVO;
import com.example.mall.vo.OrderDetailVO;
import com.example.mall.vo.OrderVO;
import com.example.mall.vo.StatisticsVO;

public interface OrderService extends IService<OrderInfo> {
    OrderCreateResultVO createOrder(OrderCreateDTO dto);

    PageResult<OrderVO> pageCurrentUserOrders(OrderQueryDTO query);

    OrderDetailVO getCurrentUserOrderDetail(Long id);

    void submitPaymentNote(Long id, PaymentNoteDTO dto);

    void cancelOrder(Long id);

    void confirmReceipt(Long id);

    PageResult<OrderVO> pageAdminOrders(OrderQueryDTO query);

    OrderDetailVO getAdminOrderDetail(Long id);

    void confirmPayment(Long id, AdminPaymentConfirmDTO dto);

    void shipOrder(Long id, ShipOrderDTO dto);

    StatisticsVO statistics();
}
