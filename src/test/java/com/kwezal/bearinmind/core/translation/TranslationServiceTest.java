package com.kwezal.bearinmind.core.translation;

import static com.kwezal.bearinmind.core.utils.AssertionUtils.assertEqualsIgnoringOrder;
import static org.junit.jupiter.api.Assertions.*;

import com.kwezal.bearinmind.core.exceptions.InvalidRequestDataException;
import com.kwezal.bearinmind.core.exceptions.ResourceNotFoundException;
import com.kwezal.bearinmind.core.translation.dto.TranslationTextDto;
import com.kwezal.bearinmind.core.translation.model.Translation;
import com.kwezal.bearinmind.core.translation.repository.TranslationRepository;
import com.kwezal.bearinmind.core.translation.service.TranslationService;
import com.kwezal.bearinmind.core.utils.CollectionUtils;
import com.kwezal.bearinmind.core.utils.TestConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/db/cleanup/TRANSLATION.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
class TranslationServiceTest {

    @Value("${application.locale}")
    private String applicationLocale;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private TranslationRepository translationRepository;

    @Test
    void Should_CreateTranslation_When_CorrectArguments() {
        // GIVEN
        final var text = "I used to be an adventurer like you, then I took an arrow in the knee";
        final var dto = new TranslationTextDto(text);

        final var expectedLocale = applicationLocale;

        // WHEN
        final var result = translationService.createTranslation(dto);

        // THEN
        assertNotNull(result.identifier());
        assertEquals(expectedLocale, result.locale());
        assertEquals(text, result.text());
    }

    @Test
    void Should_ThrowDataIntegrityViolationException_When_AttemptToCreateTranslationWithIncorrectText() {
        // GIVEN
        final var dto = new TranslationTextDto(null);

        // THEN
        assertThrows(DataIntegrityViolationException.class, () -> translationService.createTranslation(dto));
    }

    @Test
    void Should_CreateMultilingualTranslation_When_CorrectArguments() {
        // GIVEN
        final var localeTextMap = Map.of(applicationLocale, "Twenty seven", "da", "Syvogtyve");

        final var expectedIdentifier = TestConstants.TRANSLATION_IDENTIFIER_SEQUENCE_START;

        // WHEN
        final var result = translationService.createMultilingualTranslation(localeTextMap);

        // THEN
        assertEquals(expectedIdentifier, result);

        // AND
        final var translations = translationRepository.findAllByIdentifier(result);
        assertEqualsIgnoringOrder(
            localeTextMap.entrySet(),
            translations
                .stream()
                .map(translation -> Map.entry(translation.getLocale(), translation.getText()))
                .collect(Collectors.toSet())
        );
    }

    @Test
    void Should_ThrowInvalidRequestDataException_When_AttemptToCreateMultilingualTranslationWithoutApplicationLocale() {
        // GIVEN
        final var localeTextMap = Map.of("da", "Syvogtyve");

        // THEN
        assertThrows(InvalidRequestDataException.class, () -> translationService.createMultilingualTranslation(localeTextMap));
    }

    @Test
    void Should_CreateMultilingualTranslations_When_CorrectArguments() {
        // GIVEN
        final var requiredField = "required";
        final var optionalField = "optional";

        final var expectedRequiredTranslations = Map.of(applicationLocale, "It's important", "da", "Det er vigtigt");
        final var expectedOptionalTranslations = Map.of(applicationLocale, "It's less important", "pl", "To jest mniej ważne");

        final var localeFieldTextsMap = Map.of(
            applicationLocale,
            Map.of(
                requiredField,
                expectedRequiredTranslations.get(applicationLocale),
                optionalField,
                expectedOptionalTranslations.get(applicationLocale)
            ),
            "da",
            Map.of(requiredField, expectedRequiredTranslations.get("da")),
            "pl",
            Map.of(optionalField, expectedOptionalTranslations.get("pl"))
        );

        final var expectedIdentifiers = List.of(
            TestConstants.TRANSLATION_IDENTIFIER_SEQUENCE_START,
            TestConstants.TRANSLATION_IDENTIFIER_SEQUENCE_START + 1
        );

        // WHEN
        final var result = translationService.createMultilingualTranslations(
            localeFieldTextsMap,
            Set.of(requiredField),
            Set.of(optionalField)
        );

        // THEN
        assertEqualsIgnoringOrder(expectedIdentifiers, result.values());

        // AND
        final var requiredTranslations = translationRepository.findAllByIdentifier(result.get(requiredField));
        assertEqualsIgnoringOrder(
            expectedRequiredTranslations.entrySet(),
            requiredTranslations
                .stream()
                .map(translation -> Map.entry(translation.getLocale(), translation.getText()))
                .collect(Collectors.toSet())
        );

        final var optionalTranslations = translationRepository.findAllByIdentifier(result.get(optionalField));
        assertEqualsIgnoringOrder(
            expectedOptionalTranslations.entrySet(),
            optionalTranslations
                .stream()
                .map(translation -> Map.entry(translation.getLocale(), translation.getText()))
                .collect(Collectors.toSet())
        );
    }

    @ParameterizedTest
    @MethodSource(
        "Should_ThrowInvalidRequestDataException_When_AttemptToCreateMultilingualTranslationWithIncorrectArguments_Source"
    )
    void Should_ThrowInvalidRequestDataException_When_AttemptToCreateMultilingualTranslationWithIncorrectArguments(
        Map<String, Map<String, String>> localeFieldTextsMap
    ) {
        // GIVEN
        final var requiredFields = Set.of("required");
        final var optionalFields = Set.of("optional");

        // THEN
        assertThrows(
            InvalidRequestDataException.class,
            () -> translationService.createMultilingualTranslations(localeFieldTextsMap, requiredFields, optionalFields)
        );
    }

    private static Stream<Arguments> Should_ThrowInvalidRequestDataException_When_AttemptToCreateMultilingualTranslationWithIncorrectArguments_Source() {
        return Stream.of(
            Arguments.of(Map.of("da", Map.of("required", "Det er vigtigt"))),
            Arguments.of(Map.of("en", Map.of("optional", "It's less important"))),
            Arguments.of(
                Map.of(
                    "en",
                    Map.of("required", "It's important", "spanishInquisition", "Nobody expects The Spanish Inquisition")
                )
            ),
            Arguments.of(Map.of("en", Map.of("required", "It's important"), "pl", Map.of("optional", "To jest mniej ważne")))
        );
    }

    @Test
    void Should_AppendTranslation_When_CorrectArguments() {
        // GIVEN
        final var existingTranslation = createTranslation(applicationLocale, "All your bases belong to us");

        final var identifier = existingTranslation.getIdentifier();
        final var locale = "enJP";
        final var text = "All your base are belong to us";

        // WHEN
        final var result = translationService.appendTranslation(identifier, locale, text);

        // AND
        assertEquals(identifier, result.identifier());
        assertEquals(locale, result.locale());
        assertEquals(text, result.text());
    }

    @Test
    void Should_ThrowResourceNotFoundException_When_AttemptToAppendTranslationAndIdentifierDoesNotExist() {
        // GIVEN
        final var text = "Thank you for appending! But our translation is in another castle!";

        // THEN
        assertThrows(
            ResourceNotFoundException.class,
            () ->
                translationService.appendTranslation(
                    TestConstants.NONEXISTENT_TRANSLATION_IDENTIFIER,
                    TestConstants.NONEXISTENT_TRANSLATION_LOCALE,
                    text
                )
        );
    }

    @Test
    void Should_UpdateTranslation_When_CorrectArguments() {
        // GIVEN
        final var locale = applicationLocale;
        final var existingTranslation = createTranslation(locale, "The cake is a lie");

        final var text = "The cake is not a lie";

        // WHEN
        translationService.updateTranslation(existingTranslation.getIdentifier(), locale, text);

        // THEN
        final var optionalEditedTranslationText = translationRepository.findByIdentifierAndLocale(
            existingTranslation.getIdentifier(),
            existingTranslation.getLocale()
        );
        assertTrue(optionalEditedTranslationText.isPresent());

        // AND
        final var editedTranslationText = optionalEditedTranslationText.get();

        assertEquals(existingTranslation.getId(), editedTranslationText.getId());
        assertEquals(text, editedTranslationText.getText());
    }

    @ParameterizedTest
    @MethodSource("Should_ThrowResourceNotFoundException_When_AttemptToUpdateNonexistentTranslation_Source")
    void Should_ThrowResourceNotFoundException_When_AttemptToUpdateNonexistentTranslation(int identifier, String locale) {
        // GIVEN
        final var text = "Thank you for updating! But our translation is in another castle!";

        // THEN
        assertThrows(ResourceNotFoundException.class, () -> translationService.updateTranslation(identifier, locale, text));
    }

    private static Stream<Arguments> Should_ThrowResourceNotFoundException_When_AttemptToUpdateNonexistentTranslation_Source() {
        return Stream.of(
            Arguments.of(1, TestConstants.NONEXISTENT_TRANSLATION_LOCALE),
            Arguments.of(TestConstants.NONEXISTENT_TRANSLATION_IDENTIFIER, "en")
        );
    }

    @Test
    void Should_ThrowDataIntegrityViolationException_When_AttemptToUpdateTranslationWithIncorrectText() {
        // GIVEN
        final var locale = applicationLocale;
        final var existingTranslation = createTranslation(locale, "Thoughtcrime");

        // THEN
        assertThrows(
            DataIntegrityViolationException.class,
            () -> translationService.updateTranslation(existingTranslation.getIdentifier(), locale, null)
        );
    }

    @Test
    void Should_UpdateMultilingualTranslation_When_CorrectArguments() {
        // GIVEN
        final var locale = applicationLocale;
        final var existingTranslation = createTranslation(locale, "The cake is a lie");
        final var identifier = existingTranslation.getIdentifier();
        createTranslation(identifier, "it", "La torta è una bugia");

        final var expectedLocaleTextMap = Map.of(locale, "The cake is not a lie", "frBE", "Le gâteau n'est pas un mensonge");

        // WHEN
        translationService.updateMultilingualTranslation(identifier, expectedLocaleTextMap);

        // THEN
        final var editedTranslations = translationRepository.findAllByIdentifier(identifier);

        assertEquals(
            expectedLocaleTextMap,
            editedTranslations.stream().collect(Collectors.toMap(Translation::getLocale, Translation::getText))
        );
    }

    @Test
    void Should_ThrowResourceNotFoundException_When_AttemptToUpdateNonexistentMultilingualTranslation() {
        // GIVEN
        final var identifier = TestConstants.NONEXISTENT_TRANSLATION_IDENTIFIER;
        final var localeTextMap = Map.of(
            applicationLocale,
            "Thank you for updating! But our translation is in another castle!"
        );

        // THEN
        assertThrows(
            ResourceNotFoundException.class,
            () -> translationService.updateMultilingualTranslation(identifier, localeTextMap)
        );
    }

    @Test
    void Should_ThrowInvalidRequestDataException_When_AttemptToUpdateMultilingualTranslationWithIncorrectArguments() {
        // GIVEN
        final var locale = applicationLocale;
        final var existingTranslation = createTranslation(locale, "Thoughtcrime");
        final var localeTextMap = Map.of("da", "Tankeforbrydelse");

        // THEN
        assertThrows(
            InvalidRequestDataException.class,
            () -> translationService.updateMultilingualTranslation(existingTranslation.getIdentifier(), localeTextMap)
        );
    }

    @Test
    void Should_UpdateMultilingualTranslations_When_CorrectArguments() {
        // GIVEN
        final var existingField = "cake";
        final var newField = "adventurer";

        final var locale = applicationLocale;
        final var existingTranslation = createTranslation(locale, "The cake is a lie");
        final var existingIdentifier = existingTranslation.getIdentifier();
        createTranslation(existingIdentifier, "it", "La torta è una bugia");

        final var fieldIdentifierMap = new HashMap<String, Integer>();
        fieldIdentifierMap.put(existingField, existingIdentifier);
        fieldIdentifierMap.put(newField, null);

        final var localeFieldTextsMap = Map.of(
            locale,
            Map.of(
                existingField,
                "The cake is not a lie",
                newField,
                "I used to be an adventurer like you, then I took an arrow in the knee"
            ),
            "frBE",
            Map.of(existingField, "Le gâteau n'est pas un mensonge")
        );

        final var fieldLocaleTextsMap = CollectionUtils.swapMapKeys(localeFieldTextsMap);

        // WHEN
        final var editedFieldIdentifierMap = translationService.updateMultilingualTranslations(
            fieldIdentifierMap,
            localeFieldTextsMap
        );

        // THEN
        final var existingFieldTranslations = translationRepository.findAllByIdentifier(
            editedFieldIdentifierMap.get(existingField)
        );
        assertEquals(
            fieldLocaleTextsMap.get(existingField),
            existingFieldTranslations.stream().collect(Collectors.toMap(Translation::getLocale, Translation::getText))
        );

        // AND
        final var newFieldTranslations = translationRepository.findAllByIdentifier(editedFieldIdentifierMap.get(newField));
        assertEquals(
            fieldLocaleTextsMap.get(newField),
            newFieldTranslations.stream().collect(Collectors.toMap(Translation::getLocale, Translation::getText))
        );
    }

    @ParameterizedTest
    @MethodSource(
        "Should_ThrowInvalidRequestDataException_When_AttemptToUpdateMultilingualTranslationsWithIncorrectArguments_Source"
    )
    void Should_ThrowInvalidRequestDataException_When_AttemptToUpdateMultilingualTranslationsWithIncorrectArguments(
        boolean hasExistingTranslation,
        Map<String, Map<String, String>> localeFieldTextsMap
    ) {
        // GIVEN
        final var field = "cake";
        final var locale = applicationLocale;

        final Integer identifier;
        if (hasExistingTranslation) {
            final var existingTranslation = createTranslation(locale, "The cake is a lie");
            identifier = existingTranslation.getIdentifier();
        } else {
            identifier = null;
        }

        final var fieldIdentifierMap = new HashMap<String, Integer>();
        fieldIdentifierMap.put(field, identifier);

        // THEN
        assertThrows(
            InvalidRequestDataException.class,
            () -> translationService.updateMultilingualTranslations(fieldIdentifierMap, localeFieldTextsMap)
        );
    }

    private static Stream<Arguments> Should_ThrowInvalidRequestDataException_When_AttemptToUpdateMultilingualTranslationsWithIncorrectArguments_Source() {
        return Stream.of(
            Arguments.of(false, Map.of("frBE", Map.of("cake", "Le gâteau n'est pas un mensonge"))),
            Arguments.of(true, Map.of("frBE", Map.of("cake", "Le gâteau n'est pas un mensonge")))
        );
    }

    @Test
    void Should_ReturnText_When_TranslationWithGivenIdentifierAndLocaleExists() {
        // GIVEN
        final var identifier = 19;
        final var locale = "en";

        final var expectedText = "Gamification in Education";

        // WHEN
        final var result = translationService.findTextByIdentifierAndLocale(identifier, locale);

        // THEN
        assertEquals(expectedText, result);
    }

    @Test
    void Should_ThrowResourceNotFoundException_When_RequestedTranslationDoesNotExist() {
        final var identifier = TestConstants.NONEXISTENT_TRANSLATION_IDENTIFIER;
        final var locale = "en";

        // THEN
        assertThrows(
            ResourceNotFoundException.class,
            () -> translationService.findTextByIdentifierAndLocale(identifier, locale)
        );
    }

    @Test
    void Should_ReturnTranslations_When_TranslationsWithAllGivenIdentifiersAndLocaleExists() {
        // GIVEN
        final var identifiers = List.of(3, 4);
        final var locale = "en";

        final var expectedTranslations = Map.of(3, "Java", 4, "Discover the finest Indonesian coffee");

        // WHEN
        final var result = translationService.findAllIdentifierAndTextByIdentifiersAndLocale(identifiers, locale);

        // THEN
        assertEquals(expectedTranslations, result);
    }

    @Test
    void Should_DeleteTranslations_When_AtLeastOneTranslationWithGivenIdentifierExists() {
        // GIVEN
        final var firstTranslation = createTranslation("enUS", "Unwanted translation");
        final var identifier = firstTranslation.getIdentifier();
        final var secondTranslation = createTranslation(identifier, "da", "Uønsket oversættelse");

        // WHEN
        translationService.deleteAllTranslationBy(identifier);

        // THEN
        final var deletedTranslations = translationRepository.findAllById(
            List.of(firstTranslation.getId(), secondTranslation.getId())
        );

        assertTrue(deletedTranslations.isEmpty());
    }

    @Test
    void Should_DeleteTranslation_When_TranslationWithGivenIdentifierAndLocaleExists() {
        // GIVEN
        final var localeToDelete = "enUS";
        final var translationToDelete = createTranslation(localeToDelete, "Unwanted translation");
        final var identifier = translationToDelete.getIdentifier();
        final var secondTranslation = createTranslation(identifier, "da", "Uønsket oversættelse");

        // WHEN
        translationService.deleteTranslationByIdentifierAndLocale(identifier, localeToDelete);

        // THEN
        final var deletedTranslations = translationRepository.findAllById(
            List.of(translationToDelete.getId(), secondTranslation.getId())
        );

        assertEquals(1, deletedTranslations.size());
        assertNotEquals(translationToDelete.getId(), deletedTranslations.get(0).getId());
    }

    @Test
    void Should_ThrowInvalidRequestDataException_When_AttemptToDeleteTranslationWithApplicationLocale() {
        // GIVEN
        final var identifier = 1;

        // THEN
        assertThrows(
            InvalidRequestDataException.class,
            () -> translationService.deleteTranslationByIdentifierAndLocale(identifier, applicationLocale)
        );
    }

    private Translation createTranslation(String locale, String text) {
        return createTranslation(null, locale, text);
    }

    private Translation createTranslation(Integer identifier, String locale, String text) {
        return translationRepository.save(new Translation(null, identifier, locale, text));
    }
}
