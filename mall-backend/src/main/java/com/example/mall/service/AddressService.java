package com.example.mall.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.mall.dto.AddressSaveDTO;
import com.example.mall.entity.Address;

import java.util.List;

public interface AddressService extends IService<Address> {
    List<Address> listCurrentUserAddresses();

    Address createAddress(AddressSaveDTO dto);

    Address updateAddress(Long id, AddressSaveDTO dto);

    void deleteAddress(Long id);

    void setDefault(Long id);
}

