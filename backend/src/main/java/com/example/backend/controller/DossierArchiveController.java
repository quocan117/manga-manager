package com.example.backend.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.DossierArchiveDtos.DossierRoundResponse;
import com.example.backend.service.DossierArchiveService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Map;

@RestController
@RequestMapping("/api/series")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('MANGAKA', 'TANTOU_EDITOR', 'EDITORIAL_BOARD')")
@SecurityRequirement(name = "Bearer Authentication")
public class DossierArchiveController {
    private final DossierArchiveService service;

    public DossierArchiveController(DossierArchiveService service) {
        this.service = service;
    }

    @GetMapping("/{seriesId}/archive-history")
    public List<DossierRoundResponse> getArchiveHistory(@PathVariable Long seriesId) {
        return service.getArchiveHistory(seriesId);
    }

    @GetMapping("/{seriesId}/editor-workflow-history")
    public Map<String, List<Map<String, Object>>> getEditorWorkflowHistory(@PathVariable Long seriesId) {
        return service.getEditorWorkflowHistory(seriesId);
    }
}
