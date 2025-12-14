package com.kwezal.bearinmind.core.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserDto(
    @Email @NotBlank String email,

    @NotBlank String password,

    @NotBlank String firstName,

    @NotBlank String lastName,

    String middleName
) {}
