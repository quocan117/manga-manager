package com.example.backend.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.example.backend.service.SeriesFileService;
import com.example.backend.service.SeriesFileService.DownloadableSeriesFile;

@ExtendWith(MockitoExtension.class)
class SeriesFileControllerTests {
    @Mock
    private SeriesFileService service;

    @InjectMocks
    private SeriesFileController controller;

    @Test
    void downloadUsesOriginalUtf8FileNameAndMetadata() {
        byte[] content = "series dossier".getBytes(StandardCharsets.UTF_8);
        when(service.getAuthorizedDownload(10L)).thenReturn(new DownloadableSeriesFile(
                new ByteArrayResource(content),
                "hồ sơ.pdf",
                "application/pdf",
                (long) content.length));

        var response = controller.downloadSeriesFile(10L);
        String dispositionHeader = response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);

        assertEquals("hồ sơ.pdf", ContentDisposition.parse(dispositionHeader).getFilename());
        assertEquals(MediaType.APPLICATION_PDF, response.getHeaders().getContentType());
        assertEquals(content.length, response.getHeaders().getContentLength());
    }
}
