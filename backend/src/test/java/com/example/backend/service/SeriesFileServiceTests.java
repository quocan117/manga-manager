package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.model.MangaSeries;
import com.example.backend.model.Task;
import com.example.backend.model.SeriesFile;
import com.example.backend.model.User;
import com.example.backend.repository.SeriesBoardAssignmentRepository;
import com.example.backend.repository.SeriesFileRepository;

@ExtendWith(MockitoExtension.class)
class SeriesFileServiceTests {
    private static final String CURRENT_EMAIL = "reader@manga.test";

    @Mock
    private SeriesFileRepository seriesFileRepository;
    @Mock
    private SeriesBoardAssignmentRepository seriesBoardAssignmentRepository;

    @InjectMocks
    private SeriesFileService service;

    @BeforeEach
    void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(CURRENT_EMAIL, null, List.of()));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authorCanDownloadFileWithOriginalName(@TempDir Path uploadRoot) throws Exception {
        SeriesFile file = storedFile(uploadRoot, user(CURRENT_EMAIL), null);
        when(seriesFileRepository.findById(10L)).thenReturn(Optional.of(file));

        var download = service.getAuthorizedDownload(10L);

        assertEquals("hồ sơ.pdf", download.originalFileName());
        assertEquals("application/pdf", download.contentType());
        assertTrue(download.resource().exists());
    }

    @Test
    void authorCanStillDownloadAnInactiveHistoricalFile(@TempDir Path uploadRoot) throws Exception {
        SeriesFile file = storedFile(uploadRoot, user(CURRENT_EMAIL), null);
        file.setActive(false);
        when(seriesFileRepository.findById(10L)).thenReturn(Optional.of(file));

        var download = service.getAuthorizedDownload(10L);

        assertTrue(download.resource().isReadable());
        assertEquals("hồ sơ.pdf", download.originalFileName());
    }

    @Test
    void assignedEditorCanDownloadFile(@TempDir Path uploadRoot) throws Exception {
        SeriesFile file = storedFile(uploadRoot, user("author@manga.test"), user(CURRENT_EMAIL));
        when(seriesFileRepository.findById(10L)).thenReturn(Optional.of(file));

        var download = service.getAuthorizedDownload(10L);

        assertTrue(download.resource().isReadable());
    }

    @Test
    void assignedAssistantCanDownloadTaskFile(@TempDir Path uploadRoot) throws Exception {
        SeriesFile file = storedFile(uploadRoot, user("author@manga.test"), null);
        Task task = new Task();
        task.setAssignedTo(user(CURRENT_EMAIL));
        file.setTask(task);
        when(seriesFileRepository.findById(10L)).thenReturn(Optional.of(file));

        var download = service.getAuthorizedDownload(10L);

        assertTrue(download.resource().isReadable());
    }

    @Test
    void assignedBoardMemberCanDownloadFile(@TempDir Path uploadRoot) throws Exception {
        SeriesFile file = storedFile(uploadRoot, user("author@manga.test"), null);
        when(seriesFileRepository.findById(10L)).thenReturn(Optional.of(file));
        when(seriesBoardAssignmentRepository
                .existsBySeriesSeriesIdAndBoardMemberEmailIgnoreCase(20L, CURRENT_EMAIL))
                .thenReturn(true);

        var download = service.getAuthorizedDownload(10L);

        assertTrue(download.resource().isReadable());
        verify(seriesBoardAssignmentRepository)
                .existsBySeriesSeriesIdAndBoardMemberEmailIgnoreCase(20L, CURRENT_EMAIL);
    }

    @Test
    void unrelatedUserCannotDownloadFile(@TempDir Path uploadRoot) throws Exception {
        SeriesFile file = storedFile(uploadRoot, user("author@manga.test"), null);
        when(seriesFileRepository.findById(10L)).thenReturn(Optional.of(file));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getAuthorizedDownload(10L));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());
    }

    @Test
    void rejectsStoredPathOutsideUploadRoot(@TempDir Path uploadRoot) {
        ReflectionTestUtils.setField(service, "seriesFileUploadRootOverride", uploadRoot.toString());
        SeriesFile file = seriesFile(user(CURRENT_EMAIL), null);
        file.setFileUrl("series-files/../outside.pdf");
        when(seriesFileRepository.findById(10L)).thenReturn(Optional.of(file));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getAuthorizedDownload(10L));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void identifiesOnlySupportedPreviewTypes() {
        SeriesFile file = new SeriesFile();
        file.setFileId(10L);
        file.setContentType("Application/PDF");

        assertTrue(SeriesFileSupport.isPreviewable(file));
        assertEquals("/series-files/10/download", SeriesFileSupport.downloadUrl(file));

        file.setContentType("application/zip");
        assertFalse(SeriesFileSupport.isPreviewable(file));
    }

    private SeriesFile storedFile(Path uploadRoot, User author, User editor) throws Exception {
        ReflectionTestUtils.setField(service, "seriesFileUploadRootOverride", uploadRoot.toString());
        Path seriesFolder = Files.createDirectories(uploadRoot.resolve("series-20"));
        byte[] content = "series dossier".getBytes(StandardCharsets.UTF_8);
        Files.write(seriesFolder.resolve("stored.pdf"), content);

        SeriesFile file = seriesFile(author, editor);
        file.setFileSize((long) content.length);
        return file;
    }

    private SeriesFile seriesFile(User author, User editor) {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        series.setAuthor(author);
        series.setTantouEditor(editor);

        SeriesFile file = new SeriesFile();
        file.setFileId(10L);
        file.setSeries(series);
        file.setOriginalFileName("hồ sơ.pdf");
        file.setFileName("stored.pdf");
        file.setFileUrl("series-files/series-20/stored.pdf");
        file.setContentType("application/pdf");
        file.setActive(true);
        return file;
    }

    private User user(String email) {
        User user = new User();
        user.setEmail(email);
        return user;
    }
}
