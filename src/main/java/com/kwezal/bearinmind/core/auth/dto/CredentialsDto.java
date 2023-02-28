package com.kwezal.bearinmind.core.auth.dto;

import javax.validation.constraints.NotBlank;

public record CredentialsDto(
    @NotBlank String username,

    @NotBlank String password
) {}
