package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.OrderInfo;
import com.example.mall.vo.UserOrderSummaryVO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Update("""
            UPDATE order_info
            SET payment_note = #{paymentNote},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
              AND user_id = #{userId}
              AND status = 0
            """)
    int updatePaymentNoteIfWaiting(@Param("id") Long id,
                                   @Param("userId") Long userId,
                                   @Param("paymentNote") String paymentNote);

    @Update("""
            UPDATE order_info
            SET status = 1,
                pay_time = #{payTime},
                admin_remark = #{adminRemark},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
              AND status = 0
            """)
    int confirmPaymentIfWaiting(@Param("id") Long id,
                                @Param("payTime") LocalDateTime payTime,
                                @Param("adminRemark") String adminRemark);

    @Update("""
            UPDATE order_info
            SET status = 4,
                cancel_time = #{cancelTime},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
              AND user_id = #{userId}
              AND status = 0
            """)
    int cancelIfWaitingPayment(@Param("id") Long id,
                               @Param("userId") Long userId,
                               @Param("cancelTime") LocalDateTime cancelTime);

    @Update("""
            UPDATE order_info
            SET status = 2,
                shipping_no = #{shippingNo},
                admin_remark = #{adminRemark},
                ship_time = #{shipTime},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
              AND status = 1
            """)
    int shipIfWaitingShipment(@Param("id") Long id,
                              @Param("shippingNo") String shippingNo,
                              @Param("adminRemark") String adminRemark,
                              @Param("shipTime") LocalDateTime shipTime);

    @Update("""
            UPDATE order_info
            SET status = 3,
                finish_time = #{finishTime},
                confirm_time = #{confirmTime},
                update_time = CURRENT_TIMESTAMP
            WHERE id = #{id}
              AND user_id = #{userId}
              AND status = 2
            """)
    int confirmReceiptIfShipped(@Param("id") Long id,
                                @Param("userId") Long userId,
                                @Param("finishTime") LocalDateTime finishTime,
                                @Param("confirmTime") LocalDateTime confirmTime);
}
