package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.OrderInfo;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    @Select("SELECT COUNT(*) FROM order_info")
    Long countAllOrders();

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM order_info WHERE status IN (1, 2, 3)")
    BigDecimal sumPaidAmount();

    @Select("SELECT COUNT(*) FROM order_info WHERE status = 1")
    Long countWaitingShipOrders();
}

