package com.example.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.common.PageResult;
import com.example.mall.dto.ProductQueryDTO;
import com.example.mall.dto.ProductSaveDTO;
import com.example.mall.entity.Product;
import com.example.mall.vo.ProductVO;

public interface ProductService extends IService<Product> {
    PageResult<ProductVO> pageProducts(ProductQueryDTO query, boolean admin);

    ProductVO getProductDetail(Long id, boolean admin);

    ProductVO createProduct(ProductSaveDTO dto);

    ProductVO updateProduct(Long id, ProductSaveDTO dto);

    void deleteProduct(Long id);

    void updateProductStatus(Long id, Integer status);

    void updateProductStock(Long id, Integer stock);
}
