package com.kwezal.bearinmind.core.translation.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.kwezal.bearinmind.core.config.ApplicationConfig;
import com.kwezal.bearinmind.core.translation.dto.TranslationDto;
import com.kwezal.bearinmind.core.translation.dto.TranslationIdentifierAndLocaleDto;
import com.kwezal.bearinmind.core.translation.dto.TranslationIdentifierAndTextDto;
import com.kwezal.bearinmind.core.translation.dto.TranslationTextDto;
import com.kwezal.bearinmind.core.translation.mapper.TranslationMapper;
import com.kwezal.bearinmind.core.translation.model.Translation;
import com.kwezal.bearinmind.core.translation.model.Translation_;
import com.kwezal.bearinmind.core.translation.repository.TranslationRepository;
import com.kwezal.bearinmind.core.utils.CollectionUtils;
import com.kwezal.bearinmind.core.validation.annotation.Locale;
import com.kwezal.bearinmind.exception.InvalidRequestDataException;
import com.kwezal.bearinmind.exception.ResourceNotFoundException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class TranslationService {

    private final ApplicationConfig applicationConfig;

    private final TranslationRepository translationRepository;
    private final TranslationMapper translationMapper;
    private final TranslationValidationService translationValidationService;

    @Transactional(readOnly = false)
    public TranslationDto createTranslation(final TranslationTextDto dto) {
        final var applicationLocale = applicationConfig.getApplicationLocale();
        var translation = translationMapper.map(dto, applicationLocale);
        translation = translationRepository.save(translation);
        return translationMapper.mapToTranslationDto(translation);
    }

    /**
     * Creates a single translation in multiple locales.
     * Text can only be defined if it is also present in the application locale.
     *
     * @param localeTextMap mapping of locale to text
     * @return created translation's identifier or {@code null} if a given mapping is empty
     */
    @Transactional(readOnly = false)
    public Integer createMultilingualTranslation(
        final Map<@com.kwezal.bearinmind.core.validation.annotation.Locale String, String> localeTextMap
    ) {
        return createMultilingualTranslation(localeTextMap, false);
    }

    /**
     * Creates a single translation in multiple locales.
     * Text can only be defined if it is also present in the application locale.
     *
     * @param localeTextMap mapping of locale to text
     * @param isRequired    flag that specifies whether a given mapping can be empty
     * @return created translation's identifier or {@code null} if a given mapping is empty
     */
    @Transactional(readOnly = false)
    public Integer createMultilingualTranslation(
        final Map<@com.kwezal.bearinmind.core.validation.annotation.Locale String, String> localeTextMap,
        final boolean isRequired
    ) {
        if (!isRequired && isEmpty(localeTextMap)) {
            return null;
        }

        final var applicationLocale = applicationConfig.getApplicationLocale();
        translationValidationService.validateIfTranslationsInLocaleExist(localeTextMap, applicationLocale);

        // Make a copy to avoid modifying the passed argument
        final var localeTextWithoutApplicationLocaleMap = new HashMap<>(localeTextMap);

        final var applicationLocaleText = localeTextWithoutApplicationLocaleMap.remove(applicationLocale);

        final var translation = translationRepository.save(translationMapper.map(applicationLocaleText, applicationLocale));
        final var identifier = translation.getIdentifier();

        if (!localeTextWithoutApplicationLocaleMap.isEmpty()) {
            final var translations = translationMapper.map(localeTextWithoutApplicationLocaleMap, identifier);
            translationRepository.saveAll(translations);
        }

        return identifier;
    }

    /**
     * Creates multiple translations in multiple locales.
     * Required fields have to be present in the application locale.
     * Optional fields can only be defined if they are also present in the application locale.
     * Fields other than required and optional are not allowed.
     *
     * @param localeFieldTextsMap mapping of locale to field texts
     * @param requiredFields      required field keys
     * @param optionalFields      optional field keys
     * @return mapping of field key to created translation's identifier or an empty map if a given mapping and required fields are empty
     */
    @Transactional(readOnly = false)
    public Map<String, Integer> createMultilingualTranslations(
        final Map<@com.kwezal.bearinmind.core.validation.annotation.Locale String, Map<String, String>> localeFieldTextsMap,
        final Set<String> requiredFields,
        final Set<String> optionalFields
    ) {
        if (localeFieldTextsMap.isEmpty() && requiredFields.isEmpty()) {
            return Map.of();
        }

        final var applicationLocale = applicationConfig.getApplicationLocale();
        translationValidationService.validateIfTranslationsInLocaleExist(localeFieldTextsMap, applicationLocale);

        // Make a copy to avoid modifying the passed argument
        final var localeTextWithoutApplicationLocaleMap = new HashMap<>(localeFieldTextsMap);

        final var applicationLocaleFieldTextMap = localeTextWithoutApplicationLocaleMap.remove(applicationLocale);

        translationValidationService.validateIfTranslationsHaveRequiredFields(applicationLocaleFieldTextMap, requiredFields);
        translationValidationService.validateIfTranslationsContainOnlyExpectedFields(
            applicationLocaleFieldTextMap,
            requiredFields,
            optionalFields
        );
        translationValidationService.validateIfFieldIsNotDefinedIfNotPresentInLocale(
            localeTextWithoutApplicationLocaleMap,
            applicationLocaleFieldTextMap
        );

        final var fieldIdentifiers = createApplicationLocaleTranslations(applicationLocaleFieldTextMap);
        if (!localeTextWithoutApplicationLocaleMap.isEmpty()) {
            final var translations = translationMapper.map(localeTextWithoutApplicationLocaleMap, fieldIdentifiers);
            translationRepository.saveAll(translations);
        }

        return fieldIdentifiers;
    }

    private Map<String, Integer> createApplicationLocaleTranslations(final Map<String, String> applicationLocaleFieldTextMap) {
        final var applicationLocale = applicationConfig.getApplicationLocale();
        return applicationLocaleFieldTextMap
            .entrySet()
            .stream()
            .map(field -> {
                final var translation = translationRepository.save(translationMapper.map(field.getValue(), applicationLocale));
                return Map.entry(field.getKey(), translation.getIdentifier());
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Transactional(readOnly = false)
    public TranslationDto appendTranslation(final Integer identifier, final String locale, final String text) {
        requireExistsByIdentifier(identifier);
        var translation = translationMapper.map(text, locale, identifier);
        translation = translationRepository.save(translation);
        return translationMapper.mapToTranslationDto(translation);
    }

    @Transactional(readOnly = false)
    public void updateTranslation(final Integer identifier, final String locale, final String text) {
        final var translation = fetchTranslationByIdentifierAndLocale(identifier, locale);
        translation.setText(text);
        translationRepository.save(translation);
    }

    @Transactional(readOnly = false)
    public void updateMultilingualTranslation(
        final Integer identifier,
        final Map<@com.kwezal.bearinmind.core.validation.annotation.Locale String, String> localeTextMap
    ) {
        final var applicationLocale = applicationConfig.getApplicationLocale();
        translationValidationService.validateIfTranslationsInLocaleExist(localeTextMap, applicationLocale);

        final var translations = translationRepository.findAllByIdentifier(identifier);
        if (translations.isEmpty()) {
            throw new ResourceNotFoundException(Translation.class, Map.of(Translation_.IDENTIFIER, identifier));
        }

        // Make a copy to avoid modifying the passed argument
        final var localeTextToCreateMap = new HashMap<>(localeTextMap);

        final var translationsToSave = new ArrayList<Translation>();
        final var translationsToDelete = new ArrayList<Translation>();

        for (final var translation : translations) {
            if (localeTextToCreateMap.containsKey(translation.getLocale())) {
                final var text = localeTextToCreateMap.remove(translation.getLocale());
                if (!translation.getText().equals(text)) {
                    translation.setText(text);
                    translationsToSave.add(translation);
                }
            } else {
                translationsToDelete.add(translation);
            }
        }

        if (!translationsToDelete.isEmpty()) {
            translationRepository.deleteAll(translationsToDelete);
        }

        if (!localeTextToCreateMap.isEmpty()) {
            final var translationsToCreate = translationMapper.map(localeTextToCreateMap, identifier);
            translationsToSave.addAll(translationsToCreate);
        }

        if (!translationsToSave.isEmpty()) {
            translationRepository.saveAll(translationsToSave);
        }
    }

    @Transactional(readOnly = false)
    public Map<String, Integer> updateMultilingualTranslations(
        final Map<String, Integer> fieldIdentifierMap,
        final Map<@Locale String, Map<String, String>> localeFieldTextsMap
    ) {
        // Make a copy to avoid modifying the passed argument
        final var result = new HashMap<>(fieldIdentifierMap);

        final var fieldLocaleTextsMap = CollectionUtils.swapMapKeys(localeFieldTextsMap);

        fieldIdentifierMap.forEach((field, identifier) -> {
            if (isNull(identifier)) {
                final var createdIdentifier = createMultilingualTranslation(fieldLocaleTextsMap.get(field));
                if (nonNull(createdIdentifier)) {
                    result.put(field, createdIdentifier);
                }
            } else {
                updateMultilingualTranslation(identifier, fieldLocaleTextsMap.get(field));
            }
        });

        return result;
    }

    public String findTextByIdentifierAndLocale(final Integer identifier, final String locale) {
        final var applicationLocale = applicationConfig.getApplicationLocale();
        final var text = translationRepository.findTextByIdentifierAndLocaleOrDefaultLocale(
            identifier,
            locale,
            applicationLocale
        );

        return text.orElseThrow(() ->
            new ResourceNotFoundException(
                Translation.class,
                Map.of(
                    Translation_.IDENTIFIER,
                    identifier,
                    Translation_.LOCALE,
                    applicationLocale.equals(locale) ? List.of(locale) : List.of(locale, applicationLocale)
                )
            )
        );
    }

    public <T> Map<Integer, String> findAllIdentifierAndTextByIdentifiersAndLocale(
        final Stream<T> stream,
        final Function<T, Integer> mapper,
        final String locale
    ) {
        final var identifiers = stream.map(mapper).collect(Collectors.toSet());
        return findAllIdentifierAndTextByIdentifiersAndLocale(identifiers, locale);
    }

    public Map<Integer, String> findAllIdentifierAndTextByIdentifiersAndLocale(
        final Collection<Integer> identifiers,
        final String locale
    ) {
        if (isEmpty(identifiers)) {
            return Map.of();
        }

        final var applicationLocale = applicationConfig.getApplicationLocale();
        final var texts = translationRepository.findAllIdentifierAndTextByIdentifiersAndLocaleOrDefaultLocale(
            identifiers,
            locale,
            applicationLocale
        );

        return texts
            .stream()
            .collect(Collectors.toMap(TranslationIdentifierAndTextDto::identifier, TranslationIdentifierAndTextDto::text));
    }

    @Transactional(readOnly = false)
    public void deleteAllTranslationBy(final Integer identifier) {
        translationRepository.deleteAllByIdentifier(identifier);
    }

    @Transactional(readOnly = false)
    public void deleteTranslationByIdentifierAndLocale(final Integer identifier, final String locale) {
        // Prevent deletion of the translation in the default locale of the application
        final var applicationLocale = applicationConfig.getApplicationLocale();
        if (applicationLocale.equals(locale)) {
            throw new InvalidRequestDataException(TranslationIdentifierAndLocaleDto.class, Map.of("locale", locale));
        }

        translationRepository.deleteByIdentifierAndLocale(identifier, locale);
    }

    private Translation fetchTranslationByIdentifierAndLocale(final Integer identifier, final String locale) {
        return translationRepository
            .findByIdentifierAndLocale(identifier, locale)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    Translation.class,
                    Map.of(Translation_.IDENTIFIER, identifier, Translation_.LOCALE, locale)
                )
            );
    }

    private void requireExistsByIdentifier(final Integer identifier) {
        if (!translationRepository.existsByIdentifier(identifier)) {
            throw new ResourceNotFoundException(Translation.class, Map.of(Translation_.IDENTIFIER, identifier.toString()));
        }
    }
}
