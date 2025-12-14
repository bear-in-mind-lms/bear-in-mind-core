package com.kwezal.bearinmind.core.user.dto;

import com.kwezal.bearinmind.translation.validation.annotation.Locale;
import jakarta.validation.constraints.NotBlank;

public record UpdateUserDto(
    @NotBlank String firstName,

    String middleName,

    @NotBlank String lastName,

    String title,

    @Locale String locale,

    String phoneNumber
) {}
