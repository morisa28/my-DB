package com.example.mall.service.impl;

import com.example.mall.config.UploadProperties;
import com.example.mall.exception.BusinessException;
import com.example.mall.service.FileUploadService;
import com.example.mall.vo.UploadResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LocalFileUploadServiceImpl implements FileUploadService {
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final UploadProperties uploadProperties;

    @Override
    public UploadResultVO uploadProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        if (file.getSize() > uploadProperties.getMaxSizeBytes()) {
            throw new BusinessException("图片大小不能超过2MB");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        if (!isAllowedExtension(extension)) {
            throw new BusinessException("仅支持 jpg、jpeg、png、webp 图片");
        }
        String contentType = file.getContentType();
        if (contentType != null && !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("文件类型不是受支持的图片格式");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("读取上传文件失败");
        }
        if (!matchesImageSignature(extension, bytes)) {
            throw new BusinessException("图片文件内容与扩展名不匹配");
        }

        Path uploadRoot = Path.of(uploadProperties.getDir()).toAbsolutePath().normalize();
        String month = LocalDate.now().format(MONTH_FORMATTER);
        Path targetDir = uploadRoot.resolve("product").resolve(month).normalize();
        if (!targetDir.startsWith(uploadRoot)) {
            throw new BusinessException("上传目录配置不合法");
        }

        String filename = System.currentTimeMillis() + "-" + UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path target = targetDir.resolve(filename).normalize();
        if (!target.startsWith(uploadRoot)) {
            throw new BusinessException("上传文件路径不合法");
        }

        try {
            Files.createDirectories(targetDir);
            Files.write(target, bytes);
        } catch (IOException e) {
            throw new BusinessException("保存上传文件失败");
        }

        String baseUrl = normalizeBaseUrl(uploadProperties.getBaseUrl());
        return new UploadResultVO(baseUrl + "/product/" + month + "/" + filename, filename, file.getSize());
    }

    private String resolveExtension(String originalFilename) {
        String filename = StringUtils.cleanPath(originalFilename == null ? "" : originalFilename);
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            throw new BusinessException("图片文件必须包含扩展名");
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedExtension(String extension) {
        return uploadProperties.getAllowedExtensions().stream()
                .map(value -> value.toLowerCase(Locale.ROOT))
                .anyMatch(value -> value.equals(extension));
    }

    private boolean matchesImageSignature(String extension, byte[] bytes) {
        return switch (extension) {
            case "jpg", "jpeg" -> bytes.length >= 3
                    && (bytes[0] & 0xFF) == 0xFF
                    && (bytes[1] & 0xFF) == 0xD8
                    && (bytes[2] & 0xFF) == 0xFF;
            case "png" -> bytes.length >= 8
                    && (bytes[0] & 0xFF) == 0x89
                    && bytes[1] == 0x50
                    && bytes[2] == 0x4E
                    && bytes[3] == 0x47
                    && bytes[4] == 0x0D
                    && bytes[5] == 0x0A
                    && bytes[6] == 0x1A
                    && bytes[7] == 0x0A;
            case "webp" -> bytes.length >= 12
                    && bytes[0] == 0x52
                    && bytes[1] == 0x49
                    && bytes[2] == 0x46
                    && bytes[3] == 0x46
                    && bytes[8] == 0x57
                    && bytes[9] == 0x45
                    && bytes[10] == 0x42
                    && bytes[11] == 0x50;
            default -> false;
        };
    }

    private String normalizeBaseUrl(String baseUrl) {
        String value = StringUtils.hasText(baseUrl) ? baseUrl.trim() : "/uploads";
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }
}
