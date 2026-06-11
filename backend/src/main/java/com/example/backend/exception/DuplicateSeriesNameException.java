package com.example.backend.exception;

public class DuplicateSeriesNameException extends RuntimeException {

    public DuplicateSeriesNameException(String name) {
        super("Tên series đã tồn tại: " + name);
    }
}
