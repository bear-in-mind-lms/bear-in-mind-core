package com.kwezal.bearinmind.core.translation.dto;

import com.kwezal.bearinmind.core.validation.annotation.Locale;
import javax.validation.constraints.NotNull;

public record TranslationDto(
    @NotNull Long id,

    @NotNull Integer identifier,

    @Locale String locale,

    @NotNull String text
) {}
