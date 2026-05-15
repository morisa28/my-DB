package com.example.mall.security;

import com.example.mall.exception.BusinessException;

public final class UserContext {
    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        LoginUser user = HOLDER.get();
        if (user == null) {
            throw new BusinessException(401, "未登录或登录已失效");
        }
        return user;
    }

    public static Long userId() {
        return get().getId();
    }

    public static boolean isAdmin() {
        return Integer.valueOf(1).equals(get().getRole());
    }

    public static void clear() {
        HOLDER.remove();
    }
}

