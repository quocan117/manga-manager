package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CharacterRequest {

    @NotBlank(message = "Tên nhân vật không được để trống")
    @Size(max = 100, message = "Tên nhân vật tối đa 100 ký tự")
    private String name;

    @Size(max = 500, message = "Mô tả nhân vật tối đa 500 ký tự")
    private String description;
}
