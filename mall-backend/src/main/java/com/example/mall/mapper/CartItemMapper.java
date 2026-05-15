package com.example.mall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.mall.entity.CartItem;
import com.example.mall.vo.CartItemVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CartItemMapper extends BaseMapper<CartItem> {

    @Select("""
            SELECT ci.id,
                   ci.product_id AS productId,
                   p.name AS productName,
                   p.price AS productPrice,
                   p.image_url AS productImage,
                   p.status AS productStatus,
                   p.stock,
                   ci.quantity,
                   p.price * ci.quantity AS totalPrice
            FROM cart_item ci
            INNER JOIN product p ON ci.product_id = p.id
            WHERE ci.user_id = #{userId}
            ORDER BY ci.create_time DESC
            """)
    List<CartItemVO> selectCartItemsByUserId(Long userId);
}

