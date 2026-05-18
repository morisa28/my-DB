package com.example.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.common.PageResult;
import com.example.mall.dto.LoginDTO;
import com.example.mall.dto.PasswordUpdateDTO;
import com.example.mall.dto.RegisterDTO;
import com.example.mall.dto.UserUpdateDTO;
import com.example.mall.entity.User;
import com.example.mall.vo.LoginVO;
import com.example.mall.vo.UserOrderSummaryVO;
import com.example.mall.vo.UserVO;

public interface UserService extends IService<User> {
    UserVO register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    UserVO currentUser();

    UserVO updateProfile(UserUpdateDTO dto);

    void updatePassword(PasswordUpdateDTO dto);

    PageResult<UserVO> pageUsers(Long page, Long size, String keyword);

    void updateUserStatus(Long id, Integer status);

    UserOrderSummaryVO getUserOrderSummary(Long id);
}
