package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.dto.CartAddDTO;
import com.example.mall.dto.CartUpdateDTO;
import com.example.mall.service.CartService;
import com.example.mall.vo.CartItemVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart")
public class CartController {
    private final CartService cartService;

    @GetMapping
    public Result<List<CartItemVO>> list() {
        return Result.ok(cartService.listCurrentCart());
    }

    @GetMapping("/count")
    public Result<Map<String, Long>> count() {
        return Result.ok(Map.of("count", cartService.countCurrentCart()));
    }

    @PostMapping
    public Result<Void> add(@Valid @RequestBody CartAddDTO dto) {
        cartService.addCartItem(dto);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CartUpdateDTO dto) {
        cartService.updateQuantity(id, dto.getQuantity());
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        cartService.deleteCartItem(id);
        return Result.ok();
    }

    @DeleteMapping("/clear")
    public Result<Void> clear() {
        cartService.clearCart();
        return Result.ok();
    }
}

