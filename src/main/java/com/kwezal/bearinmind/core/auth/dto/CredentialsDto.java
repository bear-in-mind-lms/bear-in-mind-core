package com.kwezal.bearinmind.core.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record CredentialsDto(
    @NotBlank String username,

    @NotBlank String password
) {}
