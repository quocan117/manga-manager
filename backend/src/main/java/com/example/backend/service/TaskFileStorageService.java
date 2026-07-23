package com.example.backend.service;

import com.example.backend.model.MangaSeries;
import com.example.backend.model.SeriesFile;
import com.example.backend.model.Task;
import com.example.backend.model.User;
import com.example.backend.repository.SeriesFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class TaskFileStorageService {
    public static final String TASK_ORIGINAL = "TASK_ORIGINAL";
    public static final String TASK_SUBMISSION = "TASK_SUBMISSION";

    private static final long MAX_FILE_SIZE_BYTES = 20L * 1024 * 1024;
    private static final long MAX_ZIP_FILE_SIZE_BYTES = 100L * 1024 * 1024;
    private static final long MAX_TOTAL_SIZE_BYTES = 200L * 1024 * 1024;
    private static final int MAX_FILES = 20;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif",
            ".pdf", ".txt", ".md", ".doc", ".docx", ".zip");

    private final SeriesFileRepository seriesFileRepository;

    @Value("${manga.upload.series-file-root:}")
    private String uploadRootOverride;

    public TaskFileStorageService(SeriesFileRepository seriesFileRepository) {
        this.seriesFileRepository = seriesFileRepository;
    }

    public List<SeriesFile> storeTaskFiles(
            Task task,
            User uploadedBy,
            List<MultipartFile> files,
            Integer roundNumber,
            String purpose) {
        List<MultipartFile> validFiles = validateAndRequireFiles(files);
        MangaSeries series = task.getChapter() == null ? null : task.getChapter().getSeries();
        if (task.getTaskId() == null || series == null || series.getSeriesId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Task must be saved and linked to a manga series before files are stored");
        }

        Path relativeFolder = Path.of(
                "series-" + series.getSeriesId(),
                "tasks",
                "task-" + task.getTaskId(),
                "round-" + roundNumber);
        Path uploadRoot = uploadRoot().resolve(relativeFolder).normalize();
        try {
            Files.createDirectories(uploadRoot);
        } catch (IOException exception) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not create task file folder",
                    exception);
        }

        List<SeriesFile> storedFiles = new ArrayList<>();
        for (MultipartFile file : validFiles) {
            String extension = fileExtension(file.getOriginalFilename());
            String storedName = "task-" + task.getTaskId()
                    + "-round-" + roundNumber
                    + "-" + UUID.randomUUID() + extension;
            Path target = uploadRoot.resolve(storedName).normalize();
            if (!target.startsWith(uploadRoot)) {
                throw badRequest("Invalid task file name");
            }

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException exception) {
                throw new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Could not save task file",
                        exception);
            }

            SeriesFile stored = new SeriesFile();
            stored.setSeries(series);
            stored.setTask(task);
            stored.setUploadedBy(uploadedBy);
            stored.setFileName(storedName);
            stored.setOriginalFileName(blankToNull(file.getOriginalFilename()));
            stored.setFileUrl("series-files/" + relativeFolder.toString().replace('\\', '/') + "/" + storedName);
            stored.setContentType(blankToNull(file.getContentType()));
            stored.setFileSize(file.getSize());
            stored.setFileType(purpose);
            stored.setRoundNumber(roundNumber);
            stored.setPurpose(purpose);
            stored.setActive(true);
            stored.setUploadedAt(LocalDateTime.now());
            storedFiles.add(seriesFileRepository.save(stored));
        }
        return storedFiles;
    }

    private List<MultipartFile> validateAndRequireFiles(List<MultipartFile> files) {
        List<MultipartFile> presentFiles = files == null
                ? List.of()
                : files.stream()
                        .filter(file -> file != null && !file.isEmpty())
                        .toList();
        if (presentFiles.isEmpty()) {
            throw badRequest("Please attach at least one task file");
        }
        if (presentFiles.size() > MAX_FILES) {
            throw badRequest("A task upload can contain at most 20 files");
        }

        long totalSize = 0L;
        int zipCount = 0;
        for (MultipartFile file : presentFiles) {
            String extension = fileExtension(file.getOriginalFilename());
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw badRequest("Only image, PDF, text, Word, or ZIP files are allowed");
            }
            if (".zip".equals(extension)) {
                zipCount++;
            }
            long maximumSize = ".zip".equals(extension)
                    ? MAX_ZIP_FILE_SIZE_BYTES
                    : MAX_FILE_SIZE_BYTES;
            if (file.getSize() > maximumSize) {
                throw new ResponseStatusException(
                        HttpStatus.PAYLOAD_TOO_LARGE,
                        ".zip".equals(extension)
                                ? "Each ZIP task file must be 100MB or smaller"
                                : "Each task file must be 20MB or smaller");
            }
            totalSize += file.getSize();
        }
        if (zipCount > 1) {
            throw badRequest("A task upload can contain at most one ZIP file");
        }
        if (totalSize > MAX_TOTAL_SIZE_BYTES) {
            throw new ResponseStatusException(
                    HttpStatus.PAYLOAD_TOO_LARGE,
                    "A task upload must be 200MB or smaller in total");
        }
        return presentFiles;
    }

    private String fileExtension(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            throw badRequest("Task file name is required");
        }
        int extensionIndex = originalFilename.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == originalFilename.length() - 1) {
            throw badRequest("Task file extension is required");
        }
        return originalFilename.substring(extensionIndex).toLowerCase(Locale.ROOT);
    }

    private Path uploadRoot() {
        if (uploadRootOverride != null && !uploadRootOverride.isBlank()) {
            return Path.of(uploadRootOverride).toAbsolutePath().normalize();
        }
        return Path.of("uploads/series-files").toAbsolutePath().normalize();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
