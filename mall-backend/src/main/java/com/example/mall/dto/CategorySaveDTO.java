package com.example.mall.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategorySaveDTO {
    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Integer sortOrder = 0;

    @Min(value = 0, message = "分类状态只能是 0 或 1")
    @Max(value = 1, message = "分类状态只能是 0 或 1")
    private Integer status = 1;
}
