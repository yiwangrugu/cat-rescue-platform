package com.catrescue.catrescueplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class FileUploadConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        File uploadPath = new File(uploadDir);
        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }

        File avatarsDir = new File(uploadPath, "avatars");
        if (!avatarsDir.exists()) {
            avatarsDir.mkdirs();
        }

        String uploadAbsolutePath = uploadPath.getAbsolutePath().replace("\\", "/");
        if (!uploadAbsolutePath.endsWith("/")) {
            uploadAbsolutePath += "/";
        }

        String avatarsAbsolutePath = avatarsDir.getAbsolutePath().replace("\\", "/");
        if (!avatarsAbsolutePath.endsWith("/")) {
            avatarsAbsolutePath += "/";
        }

        System.out.println("📁 Uploads目录: " + uploadAbsolutePath);
        System.out.println("📁 Avatars目录: " + avatarsAbsolutePath);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadAbsolutePath)
                .setCachePeriod(0);

        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + uploadAbsolutePath)
                .setCachePeriod(0);
    }
}
