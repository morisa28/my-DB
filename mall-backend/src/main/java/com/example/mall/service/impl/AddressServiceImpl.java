package com.example.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.mall.dto.AddressSaveDTO;
import com.example.mall.entity.Address;
import com.example.mall.exception.BusinessException;
import com.example.mall.mapper.AddressMapper;
import com.example.mall.security.UserContext;
import com.example.mall.service.AddressService;
import com.example.mall.utils.CopyUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Override
    public List<Address> listCurrentUserAddresses() {
        return lambdaQuery()
                .eq(Address::getUserId, UserContext.userId())
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreateTime)
                .list();
    }

    @Override
    @Transactional
    public Address createAddress(AddressSaveDTO dto) {
        Long userId = UserContext.userId();
        Address address = CopyUtils.copy(dto, Address.class);
        address.setUserId(userId);

        boolean firstAddress = lambdaQuery().eq(Address::getUserId, userId).count() == 0;
        if (firstAddress || Integer.valueOf(1).equals(dto.getIsDefault())) {
            clearDefault(userId);
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }
        save(address);
        return address;
    }

    @Override
    @Transactional
    public Address updateAddress(Long id, AddressSaveDTO dto) {
        Address address = requireOwnAddress(id);
        address.setReceiverName(dto.getReceiverName());
        address.setReceiverPhone(dto.getReceiverPhone());
        address.setProvince(dto.getProvince());
        address.setCity(dto.getCity());
        address.setDetailAddress(dto.getDetailAddress());
        if (Integer.valueOf(1).equals(dto.getIsDefault())) {
            clearDefault(UserContext.userId());
            address.setIsDefault(1);
        } else {
            address.setIsDefault(0);
        }
        updateById(address);
        return address;
    }

    @Override
    @Transactional
    public void deleteAddress(Long id) {
        Address address = requireOwnAddress(id);
        removeById(id);
        if (Integer.valueOf(1).equals(address.getIsDefault())) {
            Address next = lambdaQuery()
                    .eq(Address::getUserId, UserContext.userId())
                    .orderByDesc(Address::getCreateTime)
                    .last("LIMIT 1")
                    .one();
            if (next != null) {
                next.setIsDefault(1);
                updateById(next);
            }
        }
    }

    @Override
    @Transactional
    public void setDefault(Long id) {
        Address address = requireOwnAddress(id);
        clearDefault(UserContext.userId());
        address.setIsDefault(1);
        updateById(address);
    }

    private Address requireOwnAddress(Long id) {
        Address address = getById(id);
        if (address == null || !UserContext.userId().equals(address.getUserId())) {
            throw new BusinessException("收货地址不存在");
        }
        return address;
    }

    private void clearDefault(Long userId) {
        lambdaUpdate()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0)
                .update();
    }
}

