package com.tfm.bandas.identity.dto;

public record UserIdentityDetailsResponse(
    String id,
    String username,
    String email,
    String firstName,
    String lastName
) {}

