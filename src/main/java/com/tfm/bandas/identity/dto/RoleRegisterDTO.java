package com.tfm.bandas.identity.dto;

import jakarta.validation.constraints.NotBlank;

public record RoleRegisterDTO(
    @NotBlank String name,
    String description
) {}

