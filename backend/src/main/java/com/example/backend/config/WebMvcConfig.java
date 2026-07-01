package com.example.backend.config;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${manga.upload.page-image-root:uploads/pages}")
    private String pageImageUploadRoot;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Path.of(pageImageUploadRoot)
                .toAbsolutePath()
                .normalize()
                .toUri()
                .toString();
        if (!uploadPath.endsWith("/")) {
            uploadPath = uploadPath + "/";
        }

        registry.addResourceHandler("/covers/pages/**")
                .addResourceLocations(uploadPath);
    }
}
