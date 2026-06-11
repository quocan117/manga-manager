package com.example.backend.service;

import com.example.backend.exception.InvalidFileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("png", "jpg", "jpeg", "pdf");
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024;

    private final Path uploadRoot;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public StoredFile storeDraft(Long seriesId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File bản thảo không được để trống");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new InvalidFileException("File không hợp lệ hoặc quá lớn (tối đa 50MB)");
        }

        String originalName = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        if (originalName.contains("..")) {
            throw new InvalidFileException("Tên file không hợp lệ");
        }

        String extension = getExtension(originalName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidFileException("File không hợp lệ. Chỉ hỗ trợ PNG, JPG, JPEG, PDF");
        }

        try {
            Path seriesDir = uploadRoot.resolve(String.valueOf(seriesId));
            Files.createDirectories(seriesDir);

            String storedName = UUID.randomUUID() + "." + extension;
            Path target = seriesDir.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            return new StoredFile(originalName, target.toString(), file.getContentType(), file.getSize());
        } catch (IOException ex) {
            throw new InvalidFileException("Không thể lưu file bản thảo");
        }
    }

    public void storeDrafts(Long seriesId, List<MultipartFile> files) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                storeDraft(seriesId, file);
            }
        }
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0) {
            return "";
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }

    public record StoredFile(String fileName, String storedPath, String contentType, long fileSize) {}
}
