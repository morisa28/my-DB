package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.dto.CartAddDTO;
import com.example.mall.entity.CartItem;
import com.example.mall.entity.Product;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.CartItemMapper;
import com.example.mall.mapper.ProductMapper;
import com.example.mall.security.UserContext;
import com.example.mall.service.CartService;
import com.example.mall.vo.CartItemVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl extends ServiceImpl<CartItemMapper, CartItem> implements CartService {
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public void addCartItem(CartAddDTO dto) {
        Long userId = UserContext.userId();
        Product product = requirePurchasableProduct(dto.getProductId());
        CartItem item = lambdaQuery()
                .eq(CartItem::getUserId, userId)
                .eq(CartItem::getProductId, dto.getProductId())
                .one();
        int targetQuantity = dto.getQuantity();
        if (item != null) {
            targetQuantity += item.getQuantity();
        }
        if (targetQuantity > product.getStock()) {
            throw new BusinessException("购物车数量不能超过库存");
        }
        if (item == null) {
            item = new CartItem();
            item.setUserId(userId);
            item.setProductId(dto.getProductId());
            item.setQuantity(dto.getQuantity());
            save(item);
        } else {
            item.setQuantity(targetQuantity);
            updateById(item);
        }
    }

    @Override
    public List<CartItemVO> listCurrentCart() {
        return baseMapper.selectCartItemsByUserId(UserContext.userId());
    }

    @Override
    @Transactional
    public void updateQuantity(Long id, Integer quantity) {
        CartItem item = requireOwnCartItem(id);
        Product product = requirePurchasableProduct(item.getProductId());
        if (quantity > product.getStock()) {
            throw new BusinessException("购物车数量不能超过库存");
        }
        item.setQuantity(quantity);
        updateById(item);
    }

    @Override
    @Transactional
    public void deleteCartItem(Long id) {
        requireOwnCartItem(id);
        removeById(id);
    }

    @Override
    @Transactional
    public void clearCart() {
        remove(new LambdaQueryWrapper<CartItem>().eq(CartItem::getUserId, UserContext.userId()));
    }

    @Override
    public Long countCurrentCart() {
        return lambdaQuery().eq(CartItem::getUserId, UserContext.userId()).count();
    }

    private CartItem requireOwnCartItem(Long id) {
        CartItem item = getById(id);
        if (item == null || !UserContext.userId().equals(item.getUserId())) {
            throw new BusinessException("购物车项不存在");
        }
        return item;
    }

    private Product requirePurchasableProduct(Long productId) {
        Product product = productMapper.selectById(productId);
        if (product == null || !Integer.valueOf(1).equals(product.getStatus())) {
            throw new BusinessException("商品不存在或已下架");
        }
        return product;
    }
}

