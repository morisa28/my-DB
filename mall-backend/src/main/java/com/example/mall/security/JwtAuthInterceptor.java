package com.example.mall.security;

import com.example.mall.entity.User;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.UserMapper;
import com.example.mall.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {
    private final JwtUtils jwtUtils;
    private final UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }

        LoginUser loginUser = jwtUtils.parseToken(authorization.substring(7));
        User user = userMapper.selectById(loginUser.getId());
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(403, "用户已被禁用");
        }

        LoginUser current = new LoginUser(user.getId(), user.getUsername(), user.getRole());
        UserContext.set(current);

        boolean adminPath = request.getRequestURI().startsWith(request.getContextPath() + "/api/admin");
        boolean adminAnnotation = false;
        if (handler instanceof HandlerMethod method) {
            adminAnnotation = method.hasMethodAnnotation(RequireAdmin.class)
                    || method.getBeanType().isAnnotationPresent(RequireAdmin.class);
        }
        if ((adminPath || adminAnnotation) && !Integer.valueOf(1).equals(current.getRole())) {
            throw new BusinessException(403, "无管理员权限");
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }
}

