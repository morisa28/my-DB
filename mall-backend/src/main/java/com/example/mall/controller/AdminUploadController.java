package com.example.mall.controller;

import com.example.mall.common.Result;
import com.example.mall.security.RequireAdmin;
import com.example.mall.service.FileUploadService;
import com.example.mall.vo.UploadResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RequireAdmin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/upload")
public class AdminUploadController {
    private final FileUploadService fileUploadService;

    @PostMapping("/product-image")
    public Result<UploadResultVO> productImage(@RequestParam("file") MultipartFile file) {
        return Result.ok(fileUploadService.uploadProductImage(file));
    }
}
