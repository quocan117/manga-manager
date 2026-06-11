package com.example.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CreateSeriesRequest {

    @NotBlank(message = "Tên series không được để trống")
    @Size(max = 200, message = "Tên series tối đa 200 ký tự")
    private String name;

    @NotBlank(message = "Thể loại không được để trống")
    @Size(max = 100, message = "Thể loại tối đa 100 ký tự")
    private String genre;

    @NotBlank(message = "Tóm tắt không được để trống")
    @Size(max = 2000, message = "Tóm tắt tối đa 2000 ký tự")
    private String summary;

    @Valid
    @Size(max = 50, message = "Tối đa 50 nhân vật")
    private List<CharacterRequest> characters = new ArrayList<>();
}
