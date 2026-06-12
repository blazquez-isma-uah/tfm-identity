package com.tfm.bandas.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserIdentityResponse(
        @JsonProperty("id") String id,
        @JsonProperty("username") String username
) {}
