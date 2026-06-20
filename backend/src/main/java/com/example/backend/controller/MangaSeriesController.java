package com.example.backend.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.MangaSeriesDetailResponse;
import com.example.backend.service.MangaSeriesQueryService;

@RestController
@RequestMapping("/manga-series")
@CrossOrigin(origins = "*")
public class MangaSeriesController {
    private final MangaSeriesQueryService service;

    public MangaSeriesController(MangaSeriesQueryService service) {
        this.service = service;
    }

    @GetMapping
    public List<MangaSeriesDetailResponse> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public MangaSeriesDetailResponse getDetail(@PathVariable Long id) {
        return service.getDetail(id);
    }
}
