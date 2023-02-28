package com.kwezal.bearinmind.core.translation.mapper;

import com.kwezal.bearinmind.core.translation.dto.TranslationDto;
import com.kwezal.bearinmind.core.translation.dto.TranslationTextDto;
import com.kwezal.bearinmind.core.translation.model.Translation;
import com.kwezal.bearinmind.core.validation.annotation.Locale;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;

@Mapper
public interface TranslationMapper {
    Translation map(TranslationDto dto);

    TranslationDto mapToTranslationDto(Translation entity);

    Translation map(TranslationTextDto dto, String locale);

    Translation map(String text, String locale, Integer identifier);

    Translation map(String text, String locale);

    default List<Translation> map(Map<@Locale String, String> localeTextMap, Integer identifier) {
        return localeTextMap
            .entrySet()
            .stream()
            .map(localeText -> map(localeText.getValue(), localeText.getKey(), identifier))
            .toList();
    }

    default List<Translation> map(Map<String, String> fieldTextMap, Map<String, Integer> fieldIdentifiers, String locale) {
        return fieldTextMap
            .entrySet()
            .stream()
            .map(field -> map(field.getValue(), locale, fieldIdentifiers.get(field.getKey())))
            .toList();
    }

    default List<Translation> map(
        Map<@Locale String, Map<String, String>> localeFieldTextsMap,
        Map<String, Integer> fieldIdentifiers
    ) {
        return localeFieldTextsMap
            .entrySet()
            .stream()
            .flatMap(localeFieldTexts -> map(localeFieldTexts.getValue(), fieldIdentifiers, localeFieldTexts.getKey()).stream())
            .toList();
    }
}
