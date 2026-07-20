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

        @Value("${manga.upload.cover-image-root:uploads/covers}")
        private String coverImageUploadRoot;

        @Value("${manga.upload.chapter-revision-note-root:uploads/chapter-revision-notes}")
        private String chapterRevisionNoteUploadRoot;

        @Value("${manga.upload.series-file-root:uploads/series-files}")
        private String seriesFileUploadRoot;

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

                String coverUploadPath = Path.of(coverImageUploadRoot)
                                .toAbsolutePath()
                                .normalize()
                                .toUri()
                                .toString();
                if (!coverUploadPath.endsWith("/")) {
                        coverUploadPath = coverUploadPath + "/";
                }

                registry.addResourceHandler("/covers/series/**")
                                .addResourceLocations(coverUploadPath);

                String chapterRevisionNoteUploadPath = Path.of(chapterRevisionNoteUploadRoot)
                                .toAbsolutePath()
                                .normalize()
                                .toUri()
                                .toString();
                if (!chapterRevisionNoteUploadPath.endsWith("/")) {
                        chapterRevisionNoteUploadPath = chapterRevisionNoteUploadPath + "/";
                }

                registry.addResourceHandler("/covers/chapter-revision-notes/**")
                                .addResourceLocations(chapterRevisionNoteUploadPath);

                String seriesFileUploadPath = Path.of(seriesFileUploadRoot)
                                .toAbsolutePath()
                                .normalize()
                                .toUri()
                                .toString();
                if (!seriesFileUploadPath.endsWith("/")) {
                        seriesFileUploadPath = seriesFileUploadPath + "/";
                }

                registry.addResourceHandler("/covers/series-files/**")
                                .addResourceLocations(seriesFileUploadPath);
        }
}
