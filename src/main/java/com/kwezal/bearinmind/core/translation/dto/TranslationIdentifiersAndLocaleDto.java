package com.kwezal.bearinmind.core.translation.dto;

import com.kwezal.bearinmind.core.validation.annotation.Locale;
import java.util.List;
import javax.validation.constraints.NotEmpty;

public record TranslationIdentifiersAndLocaleDto(
    @NotEmpty List<Integer> identifiers,

    @Locale String locale
) {}
