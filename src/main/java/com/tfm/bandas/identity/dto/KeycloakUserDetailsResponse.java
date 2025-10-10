package com.tfm.bandas.identity.dto;

public record KeycloakUserDetailsResponse(
    String id,
    String username,
    String email,
    String firstName,
    String lastName
) {}

