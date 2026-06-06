package com.tfm.bandas.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserPasswordUpdateDTO(
    @JsonProperty("newPassword") String newPassword
) {}
