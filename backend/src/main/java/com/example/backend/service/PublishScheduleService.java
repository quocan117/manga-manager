package com.example.backend.service;

import org.springframework.stereotype.Service;

import com.example.backend.repository.PublishScheduleRepository;

@Service
public class PublishScheduleService {
    private final PublishScheduleRepository repository;

    public PublishScheduleService(PublishScheduleRepository repository) {
        this.repository = repository;
    }

}
