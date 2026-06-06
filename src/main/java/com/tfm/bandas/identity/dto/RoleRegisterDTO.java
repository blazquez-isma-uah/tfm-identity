package com.tfm.bandas.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record RoleRegisterDTO(
    @NotBlank @JsonProperty("name") String name,
    @JsonProperty("description") String description
) {}

