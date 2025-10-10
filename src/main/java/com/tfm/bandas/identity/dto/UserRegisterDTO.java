package com.tfm.bandas.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRegisterDTO(
        @NotBlank String username,
        @Email String email,
        @NotBlank String password,
        @NotBlank String role,      // MUSICIAN / ADMIN
        String firstName,
        String lastName
) {}
