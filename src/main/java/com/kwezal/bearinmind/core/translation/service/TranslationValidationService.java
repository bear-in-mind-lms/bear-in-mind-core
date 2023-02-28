package com.kwezal.bearinmind.core.translation.service;

import static com.kwezal.bearinmind.core.exceptions.ErrorCode.*;
import static java.util.Objects.isNull;

import com.kwezal.bearinmind.core.exceptions.InvalidRequestDataException;
import com.kwezal.bearinmind.core.validation.annotation.Locale;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
class TranslationValidationService {

    /**
     * Throws exception if a given mapping of locale to field texts does not include a given locale.
     *
     * @param localeMap mapping of locale to any type
     * @param locale    locale
     * @throws InvalidRequestDataException if validation fails
     */
    void validateIfTranslationsInLocaleExist(final Map<@Locale String, ?> localeMap, final String locale) {
        if (isNull(localeMap) || isNull(localeMap.get(locale))) {
            throw new InvalidRequestDataException(
                Map.class,
                Map.of("localeMap", Objects.toString(localeMap), "locale", locale),
                NO_APPLICATION_LOCALE_TRANSLATION,
                List.of("localeMap", "locale")
            );
        }
    }

    /**
     * Throws exception if a given mapping of field name to text does not include all of a given required fields.
     *
     * @param fieldTextMap   mapping of field name to text
     * @param requiredFields required field keys
     * @throws InvalidRequestDataException if validation fails
     */
    void validateIfTranslationsHaveRequiredFields(final Map<String, String> fieldTextMap, final Set<String> requiredFields) {
        if (!requiredFields.stream().allMatch(fieldTextMap::containsKey)) {
            throw new InvalidRequestDataException(
                Map.class,
                Map.of("fieldTextMap", fieldTextMap, "requiredFields", requiredFields),
                NO_REQUIRED_FIELD_IN_APPLICATION_LOCALE,
                List.of("fieldTextMap", "requiredFields")
            );
        }
    }

    /**
     * Throws exception if a given mapping of field name to text includes unsupported field name.
     *
     * @param fieldTextMap   mapping of field name to text
     * @param requiredFields required field keys
     * @param optionalFields optional field keys
     * @throws InvalidRequestDataException if validation fails
     */
    void validateIfTranslationsContainOnlyExpectedFields(
        final Map<String, String> fieldTextMap,
        final Set<String> requiredFields,
        final Set<String> optionalFields
    ) {
        if (
            fieldTextMap.keySet().stream().anyMatch(field -> !requiredFields.contains(field) && !optionalFields.contains(field))
        ) {
            throw new InvalidRequestDataException(
                Map.class,
                Map.of("fieldTextMap", fieldTextMap, "requiredFields", requiredFields, "optionalFields", optionalFields),
                INVALID_TRANSLATION_FIELD,
                List.of("fieldTextMap", "requiredFields", "optionalFields")
            );
        }
    }

    /**
     * Throws exception if a given mapping of locale to field texts defines a field that is not present in a given mapping of field name to text.
     *
     * @param localeFieldTextsMap mapping of locale to field texts
     * @param fieldTextMap        mapping of field name to text
     * @throws InvalidRequestDataException if validation fails
     */
    void validateIfFieldIsNotDefinedIfNotPresentInLocale(
        final Map<@Locale String, Map<String, String>> localeFieldTextsMap,
        final Map<String, String> fieldTextMap
    ) {
        if (hasOptionalFieldNotPresentInLocale(localeFieldTextsMap, fieldTextMap)) {
            throw new InvalidRequestDataException(
                Map.class,
                Map.of("localeFieldTextsMap", localeFieldTextsMap, "fieldTextMap", fieldTextMap),
                OPTIONAL_FIELD_DEFINED_BUT_NOT_PRESENT_IN_APPLICATION_LOCALE,
                List.of("localeFieldTextsMap", "fieldTextMap")
            );
        }
    }

    private boolean hasOptionalFieldNotPresentInLocale(
        final Map<String, Map<String, String>> localeFieldTextsMap,
        final Map<String, String> fieldTextMap
    ) {
        final var fieldTextsMap = localeFieldTextsMap.values();
        return fieldTextsMap
            .stream()
            .flatMap(fieldTexts -> fieldTexts.keySet().stream())
            .distinct()
            .anyMatch(key -> !fieldTextMap.containsKey(key));
    }
}
