package com.example.backend.controller;

import com.example.backend.service.SeriesFileService;
import com.example.backend.service.SeriesFileService.DownloadableSeriesFile;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/series-files")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('MANGAKA', 'ASSISTANT', 'TANTOU_EDITOR', 'EDITORIAL_BOARD')")
@SecurityRequirement(name = "Bearer Authentication")
public class SeriesFileController {
    private final SeriesFileService service;

    public SeriesFileController(SeriesFileService service) {
        this.service = service;
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadSeriesFile(@PathVariable Long fileId) {
        DownloadableSeriesFile download = service.getAuthorizedDownload(fileId);
        ResponseEntity.BodyBuilder response = ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment()
                                .filename(download.originalFileName(), StandardCharsets.UTF_8)
                                .build()
                                .toString())
                .contentType(mediaType(download.contentType()));
        if (download.fileSize() != null && download.fileSize() >= 0) {
            response.contentLength(download.fileSize());
        }
        return response.body(download.resource());
    }

    private MediaType mediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (InvalidMediaTypeException exception) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
