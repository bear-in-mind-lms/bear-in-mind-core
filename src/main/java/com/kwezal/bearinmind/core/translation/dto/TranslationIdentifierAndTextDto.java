package com.kwezal.bearinmind.core.translation.dto;

import javax.validation.constraints.NotNull;

public record TranslationIdentifierAndTextDto(
    @NotNull Integer identifier,

    @NotNull String text
) {}
