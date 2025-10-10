package com.tfm.bandas.identity.dto;

public record UserUpdateDTO(
    String username,
    String email,
    String firstName,
    String lastName
) {}
