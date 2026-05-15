package com.example.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.dto.CartAddDTO;
import com.example.mall.entity.CartItem;
import com.example.mall.vo.CartItemVO;

import java.util.List;

public interface CartService extends IService<CartItem> {
    void addCartItem(CartAddDTO dto);

    List<CartItemVO> listCurrentCart();

    void updateQuantity(Long id, Integer quantity);

    void deleteCartItem(Long id);

    void clearCart();

    Long countCurrentCart();
}

