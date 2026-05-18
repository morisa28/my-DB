package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.OrderInfo;
import com.example.mall.vo.UserOrderSummaryVO;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    @Select("SELECT COUNT(*) FROM order_info")
    Long countAllOrders();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM order_info WHERE status IN (1, 2, 3)")
    BigDecimal sumPaidAmount();

    @Select("SELECT COUNT(*) FROM order_info WHERE status = 1")
    Long countWaitingShipOrders();

    @Select("""
            SELECT #{userId} AS userId,
                   COUNT(*) AS totalOrders,
                   COALESCE(SUM(CASE WHEN status IN (1, 2, 3) THEN total_amount ELSE 0 END), 0) AS paidAmount,
                   COALESCE(SUM(CASE WHEN status = 0 THEN 1 ELSE 0 END), 0) AS waitingPaymentOrders,
                   COALESCE(SUM(CASE WHEN status = 1 THEN 1 ELSE 0 END), 0) AS waitingShipmentOrders,
                   COALESCE(SUM(CASE WHEN status = 2 THEN 1 ELSE 0 END), 0) AS shippedOrders,
                   COALESCE(SUM(CASE WHEN status = 3 THEN 1 ELSE 0 END), 0) AS finishedOrders,
                   COALESCE(SUM(CASE WHEN status = 4 THEN 1 ELSE 0 END), 0) AS canceledOrders,
                   MAX(create_time) AS lastOrderTime
            FROM order_info
            WHERE user_id = #{userId}
            """)
    UserOrderSummaryVO selectUserOrderSummary(Long userId);
}
