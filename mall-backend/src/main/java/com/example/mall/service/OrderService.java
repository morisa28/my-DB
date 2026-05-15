package com.example.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.common.PageResult;
import com.example.mall.dto.OrderCreateDTO;
import com.example.mall.dto.OrderQueryDTO;
import com.example.mall.entity.OrderInfo;
import com.example.mall.vo.OrderCreateResultVO;
import com.example.mall.vo.OrderDetailVO;
import com.example.mall.vo.OrderVO;
import com.example.mall.vo.StatisticsVO;

public interface OrderService extends IService<OrderInfo> {
    OrderCreateResultVO createOrder(OrderCreateDTO dto);

    PageResult<OrderVO> pageCurrentUserOrders(OrderQueryDTO query);

    OrderDetailVO getCurrentUserOrderDetail(Long id);

    void payOrder(Long id);

    void cancelOrder(Long id);

    PageResult<OrderVO> pageAdminOrders(OrderQueryDTO query);

    OrderDetailVO getAdminOrderDetail(Long id);

    void shipOrder(Long id);

    StatisticsVO statistics();
}

