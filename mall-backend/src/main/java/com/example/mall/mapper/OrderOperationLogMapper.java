package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.OrderOperationLog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderOperationLogMapper extends BaseMapper<OrderOperationLog> {

    @Select("""
            SELECT id, order_id, order_no, operator_id, operator_username, operator_role,
                   action, from_status, to_status, remark, create_time
            FROM order_operation_log
            WHERE order_id = #{orderId}
            ORDER BY create_time ASC, id ASC
            """)
    List<OrderOperationLog> selectByOrderId(@Param("orderId") Long orderId);
}
