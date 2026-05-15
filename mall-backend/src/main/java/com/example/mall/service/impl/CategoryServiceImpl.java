package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.dto.CategorySaveDTO;
import com.example.mall.entity.Category;
import com.example.mall.entity.Product;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.CategoryMapper;
import com.example.mall.service.CategoryService;
import com.example.mall.service.ProductService;
import com.example.mall.utils.CopyUtils;
import com.example.mall.vo.CategoryVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {
    private final ProductService productService;

    @Override
    public List<CategoryVO> listCategories(boolean admin) {
        LambdaQueryWrapper<Category> wrapper = new LambdaQueryWrapper<Category>()
                .eq(!admin, Category::getStatus, 1)
                .orderByAsc(Category::getSortOrder)
                .orderByAsc(Category::getId);
        return CopyUtils.copyList(list(wrapper), CategoryVO.class);
    }

    @Override
    @Transactional
    public CategoryVO createCategory(CategorySaveDTO dto) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder());
        category.setStatus(dto.getStatus() == null ? 1 : dto.getStatus());
        save(category);
        return CopyUtils.copy(category, CategoryVO.class);
    }

    @Override
    @Transactional
    public CategoryVO updateCategory(Long id, CategorySaveDTO dto) {
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder());
        category.setStatus(dto.getStatus() == null ? category.getStatus() : dto.getStatus());
        updateById(category);
        return CopyUtils.copy(category, CategoryVO.class);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = getById(id);
        if (category == null) {
            throw new BusinessException("分类不存在");
        }
        long productCount = productService.lambdaQuery().eq(Product::getCategoryId, id).count();
        if (productCount > 0) {
            category.setStatus(0);
            updateById(category);
        } else {
            removeById(id);
        }
    }
}

