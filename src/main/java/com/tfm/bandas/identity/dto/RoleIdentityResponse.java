package com.tfm.bandas.identity.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RoleIdentityResponse(
    @JsonProperty("id") String id,
    @JsonProperty("name") String name,
    @JsonProperty("description") String description
) {}
