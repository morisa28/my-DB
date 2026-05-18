package com.example.mall.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResultVO {
    private String url;
    private String filename;
    private Long size;
}
