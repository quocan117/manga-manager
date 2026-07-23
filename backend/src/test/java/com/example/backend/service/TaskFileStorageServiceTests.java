package com.example.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.backend.model.Chapter;
import com.example.backend.model.MangaSeries;
import com.example.backend.model.SeriesFile;
import com.example.backend.model.Task;
import com.example.backend.model.User;
import com.example.backend.repository.SeriesFileRepository;

@ExtendWith(MockitoExtension.class)
class TaskFileStorageServiceTests {
    @Mock
    private SeriesFileRepository seriesFileRepository;

    @Test
    void storesTaskFileWithTaskRoundAndPurpose(@TempDir Path uploadRoot) {
        TaskFileStorageService service = new TaskFileStorageService(seriesFileRepository);
        ReflectionTestUtils.setField(service, "uploadRootOverride", uploadRoot.toString());
        when(seriesFileRepository.save(any(SeriesFile.class))).thenAnswer(invocation -> {
            SeriesFile saved = invocation.getArgument(0);
            saved.setFileId(90L);
            return saved;
        });
        Task task = task();
        User uploader = new User();
        uploader.setUserId(5L);
        MockMultipartFile upload = new MockMultipartFile(
                "originalFiles",
                "storyboard.zip",
                "application/zip",
                "zip-data".getBytes());

        List<SeriesFile> stored = service.storeTaskFiles(
                task,
                uploader,
                List.of(upload),
                2,
                TaskFileStorageService.TASK_ORIGINAL);

        SeriesFile file = stored.get(0);
        assertEquals(90L, file.getFileId());
        assertEquals(task, file.getTask());
        assertEquals(2, file.getRoundNumber());
        assertEquals(TaskFileStorageService.TASK_ORIGINAL, file.getPurpose());
        assertTrue(file.getFileUrl().contains("/tasks/task-60/round-2/"));
        Path storedPath = uploadRoot.resolve(file.getFileUrl().substring("series-files/".length()));
        assertTrue(Files.exists(storedPath));
    }

    @Test
    void rejectsMoreThanOneZipFile() {
        TaskFileStorageService service = new TaskFileStorageService(seriesFileRepository);
        MultipartFile firstZip = mock(MultipartFile.class);
        MultipartFile secondZip = mock(MultipartFile.class);
        when(firstZip.isEmpty()).thenReturn(false);
        when(firstZip.getOriginalFilename()).thenReturn("one.zip");
        when(firstZip.getSize()).thenReturn(1L);
        when(secondZip.isEmpty()).thenReturn(false);
        when(secondZip.getOriginalFilename()).thenReturn("two.zip");
        when(secondZip.getSize()).thenReturn(1L);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.storeTaskFiles(
                        task(),
                        new User(),
                        List.of(firstZip, secondZip),
                        1,
                        TaskFileStorageService.TASK_SUBMISSION));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    private Task task() {
        MangaSeries series = new MangaSeries();
        series.setSeriesId(20L);
        Chapter chapter = new Chapter();
        chapter.setChapterId(30L);
        chapter.setSeries(series);
        Task task = new Task();
        task.setTaskId(60L);
        task.setChapter(chapter);
        task.setRoundNumber(2);
        return task;
    }
}
