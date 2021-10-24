package com.example.demo;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

/**
 * DemoAppのフォームクラス
 */
@Data
public class DemoForm {
    private MultipartFile multipartFile;
    
    private String folderId;
    
    private String userId;
    
    private String authOption;
    
    private String userName;
}