package com.example.mall.utils;

import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.stream.Collectors;

public final class CopyUtils {
    private CopyUtils() {
    }

    public static <T> T copy(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        T target = BeanUtils.instantiateClass(targetClass);
        BeanUtils.copyProperties(source, target);
        return target;
    }

    public static <T> List<T> copyList(List<?> source, Class<T> targetClass) {
        return source.stream().map(item -> copy(item, targetClass)).collect(Collectors.toList());
    }
}

