package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.OrderItem;
import com.example.mall.vo.OrderItemVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select("""
            SELECT id,
                   product_id AS productId,
                   product_name AS productName,
                   product_price AS productPrice,
                   quantity,
                   total_price AS totalPrice,
                   product_image AS productImage
            FROM order_item
            WHERE order_id = #{orderId}
            ORDER BY id ASC
            """)
    List<OrderItemVO> selectItemsByOrderId(Long orderId);
}

