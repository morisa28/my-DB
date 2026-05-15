package com.example.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.dto.CategorySaveDTO;
import com.example.mall.entity.Category;
import com.example.mall.vo.CategoryVO;

import java.util.List;

public interface CategoryService extends IService<Category> {
    List<CategoryVO> listCategories(boolean admin);

    CategoryVO createCategory(CategorySaveDTO dto);

    CategoryVO updateCategory(Long id, CategorySaveDTO dto);

    void deleteCategory(Long id);
}

