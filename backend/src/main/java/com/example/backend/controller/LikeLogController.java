package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.LikeRequest;
import com.example.backend.service.LikeService;

@RestController
@RequestMapping("/chapters")
public class LikeLogController {

    private final LikeService likeChapter;

    public LikeLogController(
            LikeService likeChapter) {
        this.likeChapter = likeChapter;
    }

    @PostMapping("/{chapterId}/likes")
    public ResponseEntity<Void> like(
            @PathVariable Long chapterId,
            @RequestBody LikeRequest request) {

        likeChapter.likeChapter(
                chapterId,
                request.getLogId());

        return ResponseEntity.ok().build();
    }
}
