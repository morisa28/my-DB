package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.common.PageResult;
import com.example.mall.dto.LoginDTO;
import com.example.mall.dto.PasswordUpdateDTO;
import com.example.mall.dto.RegisterDTO;
import com.example.mall.dto.UserUpdateDTO;
import com.example.mall.entity.User;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.OrderInfoMapper;
import com.example.mall.mapper.UserMapper;
import com.example.mall.security.UserContext;
import com.example.mall.service.UserService;
import com.example.mall.utils.CopyUtils;
import com.example.mall.utils.JwtUtils;
import com.example.mall.vo.LoginVO;
import com.example.mall.vo.UserOrderSummaryVO;
import com.example.mall.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final OrderInfoMapper orderInfoMapper;

    @Override
    @Transactional
    public UserVO register(RegisterDTO dto) {
        if (baseMapper.selectByUsername(dto.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole(0);
        user.setStatus(1);
        save(user);
        return toUserVO(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = baseMapper.selectByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        if (!Integer.valueOf(1).equals(user.getStatus())) {
            throw new BusinessException(401, "用户名或密码错误");
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getRole());
        return new LoginVO(token, toUserVO(user));
    }

    @Override
    public UserVO currentUser() {
        User user = getById(UserContext.userId());
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return toUserVO(user);
    }

    @Override
    @Transactional
    public UserVO updateProfile(UserUpdateDTO dto) {
        User user = getById(UserContext.userId());
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        updateById(user);
        return toUserVO(user);
    }

    @Override
    @Transactional
    public void updatePassword(PasswordUpdateDTO dto) {
        User user = getById(UserContext.userId());
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        updateById(user);
    }

    @Override
    public PageResult<UserVO> pageUsers(Long page, Long size, String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(StringUtils.hasText(keyword), User::getUsername, keyword)
                .orderByDesc(User::getCreateTime);
        Page<User> result = page(new Page<>(page, size), wrapper);
        return new PageResult<>(
                result.getTotal(),
                result.getCurrent(),
                result.getSize(),
                CopyUtils.copyList(result.getRecords(), UserVO.class)
        );
    }

    @Override
    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("用户状态只能是 0 或 1");
        }
        if (UserContext.userId().equals(id) && status == 0) {
            throw new BusinessException("不能禁用当前登录管理员");
        }
        User user = getById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setStatus(status);
        updateById(user);
    }

    @Override
    public UserOrderSummaryVO getUserOrderSummary(Long id) {
        if (getById(id) == null) {
            throw new BusinessException("用户不存在");
        }
        return orderInfoMapper.selectUserOrderSummary(id);
    }

    private UserVO toUserVO(User user) {
        return CopyUtils.copy(user, UserVO.class);
    }
}
