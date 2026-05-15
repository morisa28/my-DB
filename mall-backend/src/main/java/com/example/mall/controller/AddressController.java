package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.dto.AddressSaveDTO;
import com.example.mall.entity.Address;
import com.example.mall.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
public class AddressController {
    private final AddressService addressService;

    @GetMapping
    public Result<List<Address>> list() {
        return Result.ok(addressService.listCurrentUserAddresses());
    }

    @PostMapping
    public Result<Address> create(@Valid @RequestBody AddressSaveDTO dto) {
        return Result.ok(addressService.createAddress(dto));
    }

    @PutMapping("/{id}")
    public Result<Address> update(@PathVariable Long id, @Valid @RequestBody AddressSaveDTO dto) {
        return Result.ok(addressService.updateAddress(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return Result.ok();
    }

    @PutMapping("/{id}/default")
    public Result<Void> setDefault(@PathVariable Long id) {
        addressService.setDefault(id);
        return Result.ok();
    }
}

