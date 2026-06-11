package com.example.backend.controller;

import com.example.backend.dto.CreateSeriesRequest;
import com.example.backend.dto.SeriesResponse;
import com.example.backend.service.SeriesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/series")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SeriesController {

    private final SeriesService seriesService;

    /**
     * UC01 - Tạo hồ sơ series mới (JSON, không kèm file).
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SeriesResponse createSeries(@Valid @RequestBody CreateSeriesRequest request) {
        return seriesService.createSeries(request, null);
    }

    /**
     * UC01 - Tạo hồ sơ series mới kèm upload bản thảo sơ bộ.
     * Gửi multipart/form-data với part "data" (JSON) và part "drafts" (file, có thể nhiều file).
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public SeriesResponse createSeriesWithDrafts(
            @Valid @RequestPart("data") CreateSeriesRequest request,
            @RequestPart(value = "drafts", required = false) List<MultipartFile> drafts
    ) {
        return seriesService.createSeries(request, drafts);
    }

    @GetMapping
    public List<SeriesResponse> getAllSeries() {
        return seriesService.getAllSeries();
    }

    @GetMapping("/{id}")
    public SeriesResponse getSeriesById(@PathVariable Long id) {
        return seriesService.getSeriesById(id);
    }
}
