package com.tfm.bandas.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserUpdateDTO(
    @JsonProperty("email") String email,
    @JsonProperty("firstName") String firstName,
    @JsonProperty("lastName") String lastName
) {}
