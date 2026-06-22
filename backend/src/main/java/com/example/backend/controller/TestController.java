package com.example.backend.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // CỰC KỲ QUAN TRỌNG: Cho phép Frontend khác cổng (3000 hoặc 5173) được phép gọi
                            // vào
public class TestController {

    @GetMapping("/ping")
    public String pingTest() {
        return " Kết nối thành công! Backend Spring Boot đang chạy!";
    }
}