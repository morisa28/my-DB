package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.OrderIdempotency;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface OrderIdempotencyMapper extends BaseMapper<OrderIdempotency> {

    @Select("""
            SELECT *
            FROM order_idempotency
            WHERE user_id = #{userId} AND request_id = #{requestId}
            LIMIT 1
            """)
    OrderIdempotency selectByUserAndRequestId(@Param("userId") Long userId, @Param("requestId") String requestId);

    @Update("""
            UPDATE order_idempotency
            SET order_id = #{orderId}, status = 1
            WHERE user_id = #{userId}
              AND request_id = #{requestId}
              AND status = 0
            """)
    int markSucceeded(@Param("userId") Long userId, @Param("requestId") String requestId, @Param("orderId") Long orderId);
}
