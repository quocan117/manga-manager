package com.example.backend.dto;

import lombok.*;

@Getter
@Setter
public class RegistrationRequestDTO {

    private String fullName;

    private String email;

    private String portfolioUrl;

    private String introduction;

    private String phoneNumber;

    private String requestedRole;

}
