package com.example.backend.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.DrawingDtos.DrawingResponse;
import com.example.backend.dto.DrawingDtos.RevisionResponse;
import com.example.backend.dto.DrawingDtos.SaveDrawingRequest;
import com.example.backend.dto.DrawingDtos.VersionRequest;
import com.example.backend.service.MangakaDrawingService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/mangaka/pages")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('MANGAKA')")
@SecurityRequirement(name = "Bearer Authentication")
public class MangakaDrawingController {
    private final MangakaDrawingService service;

    public MangakaDrawingController(MangakaDrawingService service) {
        this.service = service;
    }

    @GetMapping("/{pageId}/drawing")
    public DrawingResponse getDrawing(@PathVariable Long pageId) {
        return service.getDrawing(pageId);
    }

    @PutMapping("/{pageId}/drawing")
    public DrawingResponse saveDrawing(
            @PathVariable Long pageId,
            @Valid @RequestBody SaveDrawingRequest request) {
        return service.saveDrawing(pageId, request);
    }

    @PostMapping("/{pageId}/drawing/finalize")
    public DrawingResponse finalizeDrawing(
            @PathVariable Long pageId,
            @Valid @RequestBody VersionRequest request) {
        return service.finalizeDrawing(pageId, request);
    }

    @GetMapping("/{pageId}/drawing/revisions")
    public List<RevisionResponse> getRevisions(@PathVariable Long pageId) {
        return service.getRevisions(pageId);
    }

    @PostMapping("/{pageId}/drawing/revisions/{revisionId}/restore")
    public DrawingResponse restoreRevision(
            @PathVariable Long pageId,
            @PathVariable Long revisionId,
            @Valid @RequestBody VersionRequest request) {
        return service.restoreRevision(pageId, revisionId, request);
    }
}
