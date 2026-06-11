package com.example.backend.exception;

public class SeriesNotFoundException extends RuntimeException {

    public SeriesNotFoundException(Long id) {
        super("Không tìm thấy series với id: " + id);
    }
}
