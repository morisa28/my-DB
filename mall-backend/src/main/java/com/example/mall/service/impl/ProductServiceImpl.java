package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.common.PageResult;
import com.example.mall.dto.ProductQueryDTO;
import com.example.mall.dto.ProductSaveDTO;
import com.example.mall.entity.Category;
import com.example.mall.entity.Product;
import com.example.mall.entity.StockMovement;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.CategoryMapper;
import com.example.mall.mapper.ProductMapper;
import com.example.mall.mapper.StockMovementMapper;
import com.example.mall.security.UserContext;
import com.example.mall.service.AdminOperationLogService;
import com.example.mall.service.ProductService;
import com.example.mall.utils.CopyUtils;
import com.example.mall.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    private static final String MOVEMENT_ADMIN_ADJUST = "ADMIN_ADJUST";
    private static final String MODULE_PRODUCT = "PRODUCT";

    private final CategoryMapper categoryMapper;
    private final StockMovementMapper stockMovementMapper;
    private final AdminOperationLogService adminOperationLogService;

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
        adminOperationLogService.record(MODULE_PRODUCT, "CREATE_PRODUCT", product.getId(), product.getName(), "创建商品");
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
        Integer beforeStock = product.getStock();
        product.setName(dto.getName());
        product.setCategoryId(dto.getCategoryId());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setImageUrl(dto.getImageUrl());
        product.setDescription(dto.getDescription());
        product.setStatus(dto.getStatus() == null ? product.getStatus() : dto.getStatus());
        updateById(product);
        recordAdminStockAdjustment(product.getId(), beforeStock, product.getStock(), "管理员编辑商品库存");
        adminOperationLogService.record(MODULE_PRODUCT, "UPDATE_PRODUCT", product.getId(), product.getName(), "更新商品资料");
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
        adminOperationLogService.record(MODULE_PRODUCT, "DISABLE_PRODUCT", product.getId(), product.getName(), "下架商品");
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
        adminOperationLogService.record(MODULE_PRODUCT, "UPDATE_PRODUCT_STATUS", product.getId(), product.getName(), "状态更新为 " + status);
    }

    @Override
    @Transactional
    public void updateProductStock(Long id, Integer stock) {
        Product product = getById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        Integer beforeStock = product.getStock();
        product.setStock(stock);
        updateById(product);
        recordAdminStockAdjustment(product.getId(), beforeStock, stock, "管理员手动调整库存");
        adminOperationLogService.record(MODULE_PRODUCT, "UPDATE_PRODUCT_STOCK", product.getId(), product.getName(), "库存更新为 " + stock);
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

    private void recordAdminStockAdjustment(Long productId, Integer beforeStock, Integer afterStock, String remark) {
        if (Objects.equals(beforeStock, afterStock)) {
            return;
        }
        StockMovement movement = new StockMovement();
        movement.setProductId(productId);
        movement.setMovementType(MOVEMENT_ADMIN_ADJUST);
        movement.setQuantity(Math.abs(afterStock - beforeStock));
        movement.setBeforeStock(beforeStock);
        movement.setAfterStock(afterStock);
        movement.setOperatorId(UserContext.userId());
        movement.setRemark(remark);
        stockMovementMapper.insert(movement);
    }
}
