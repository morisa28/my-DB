package com.example.mall.controller;

import com.example.mall.common.PageResult;
import com.example.mall.common.Result;
import com.example.mall.dto.ProductQueryDTO;
import com.example.mall.service.ProductService;
import com.example.mall.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public Result<PageResult<ProductVO>> page(ProductQueryDTO query) {
        return Result.ok(productService.pageProducts(query, false));
    }

    @GetMapping("/search")
    public Result<PageResult<ProductVO>> search(ProductQueryDTO query) {
        return Result.ok(productService.pageProducts(query, false));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.ok(productService.getProductDetail(id, false));
    }
}

