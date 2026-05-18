package com.example.mall.service;

import com.example.mall.vo.UploadResultVO;
import org.springframework.web.multipart.MultipartFile;

public interface FileUploadService {
    UploadResultVO uploadProductImage(MultipartFile file);
}
