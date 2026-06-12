package com.tfm.bandas.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserIdentityDetailsResponse(
    @JsonProperty("id") String id,
    @JsonProperty("username") String username,
    @JsonProperty("email") String email,
    @JsonProperty("firstName") String firstName,
    @JsonProperty("lastName") String lastName
) {}
