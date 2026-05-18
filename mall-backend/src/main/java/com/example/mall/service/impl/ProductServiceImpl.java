package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.common.PageResult;
import com.example.mall.dto.ProductQueryDTO;
import com.example.mall.dto.ProductSaveDTO;
import com.example.mall.entity.Category;
import com.example.mall.entity.Product;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.CategoryMapper;
import com.example.mall.mapper.ProductMapper;
import com.example.mall.service.ProductService;
import com.example.mall.utils.CopyUtils;
import com.example.mall.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    private final CategoryMapper categoryMapper;

    @Override
    public PageResult<ProductVO> pageProducts(ProductQueryDTO query, boolean admin) {
        boolean lowStockMode = admin && Boolean.TRUE.equals(query.getLowStock());
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(query.getCategoryId() != null, Product::getCategoryId, query.getCategoryId())
                .like(StringUtils.hasText(query.getKeyword()), Product::getName, query.getKeyword())
                .eq(admin && query.getStatus() != null, Product::getStatus, query.getStatus())
                .eq(!admin, Product::getStatus, 1)
                .lt(lowStockMode, Product::getStock, 10)
                .orderByAsc(lowStockMode, Product::getStock)
                .orderByAsc(lowStockMode, Product::getId)
                .orderByDesc(!lowStockMode, Product::getCreateTime);
        Page<Product> result = page(new Page<>(query.getPage(), query.getSize()), wrapper);
        List<ProductVO> records = result.getRecords().stream().map(this::toProductVO).toList();
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public ProductVO getProductDetail(Long id, boolean admin) {
        Product product = getById(id);
        if (product == null || (!admin && !Integer.valueOf(1).equals(product.getStatus()))) {
            throw new BusinessException("商品不存在或已下架");
        }
        return toProductVO(product);
    }

    @Override
    @Transactional
    public ProductVO createProduct(ProductSaveDTO dto) {
        ensureCategoryUsable(dto.getCategoryId());
        Product product = CopyUtils.copy(dto, Product.class);
        product.setSales(0);
        product.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        save(product);
        return toProductVO(product);
    }

    @Override
    @Transactional
    public ProductVO updateProduct(Long id, ProductSaveDTO dto) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        ensureCategoryUsable(dto.getCategoryId());
        product.setName(dto.getName());
        product.setCategoryId(dto.getCategoryId());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());
        product.setDescription(dto.getDescription());
        product.setStatus(dto.getStatus() == null ? product.getStatus() : dto.getStatus());
        updateById(product);
        return toProductVO(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        product.setStatus(0);
        updateById(product);
    }

    @Override
    @Transactional
    public void updateProductStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("商品状态只能是 0 或 1");
        }
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        product.setStatus(status);
        updateById(product);
    }

    @Override
    @Transactional
    public void updateProductStock(Long id, Integer stock) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        product.setStock(stock);
        updateById(product);
    }

    private void ensureCategoryUsable(Long categoryId) {
        Category category = categoryMapper.selectById(categoryId);
        if (category == null || !Integer.valueOf(1).equals(category.getStatus())) {
            throw new BusinessException("分类不存在或已停用");
        }
    }

    private ProductVO toProductVO(Product product) {
        ProductVO vo = CopyUtils.copy(product, ProductVO.class);
        Category category = categoryMapper.selectById(product.getCategoryId());
        vo.setCategoryName(category == null ? null : category.getName());
        return vo;
    }
}
