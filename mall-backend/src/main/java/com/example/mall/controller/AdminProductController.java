package com.example.mall.controller;

import com.example.mall.common.PageResult;
import com.example.mall.common.Result;
import com.example.mall.dto.ProductQueryDTO;
import com.example.mall.dto.ProductSaveDTO;
import com.example.mall.dto.ProductStockDTO;
import com.example.mall.dto.StatusDTO;
import com.example.mall.security.RequireAdmin;
import com.example.mall.service.ProductService;
import com.example.mall.vo.ProductVO;
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

@RequireAdmin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/products")
public class AdminProductController {
    private final ProductService productService;

    @GetMapping
    public Result<PageResult<ProductVO>> page(ProductQueryDTO query) {
        return Result.ok(productService.pageProducts(query, true));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.ok(productService.getProductDetail(id, true));
    }

    @PostMapping
    public Result<ProductVO> create(@Valid @RequestBody ProductSaveDTO dto) {
        return Result.ok(productService.createProduct(dto));
    }

    @PutMapping("/{id}")
    public Result<ProductVO> update(@PathVariable Long id, @Valid @RequestBody ProductSaveDTO dto) {
        return Result.ok(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.ok();
    }

    @PutMapping("/{id}/status")
    public Result<Void> status(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        productService.updateProductStatus(id, dto.getStatus());
        return Result.ok();
    }

    @PutMapping("/{id}/stock")
    public Result<Void> stock(@PathVariable Long id, @Valid @RequestBody ProductStockDTO dto) {
        productService.updateProductStock(id, dto.getStock());
        return Result.ok();
    }
}

