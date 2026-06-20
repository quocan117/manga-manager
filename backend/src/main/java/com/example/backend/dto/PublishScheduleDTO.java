package com.example.backend.dto;

import java.time.LocalDateTime;

import com.example.backend.model.MangaSeries;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class PublishScheduleDTO {
    private Long scheduleId;
    private MangaSeries series;
    private LocalDateTime publishDate;
    private String frequency;
    private String status;

}
