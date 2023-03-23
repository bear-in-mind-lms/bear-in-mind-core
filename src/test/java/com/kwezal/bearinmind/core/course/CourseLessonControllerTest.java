package com.kwezal.bearinmind.core.course;

import static com.kwezal.bearinmind.core.utils.AssertionUtils.assertEqualsIgnoringOrder;
import static com.kwezal.bearinmind.core.utils.AssertionUtils.assertTimeDifferenceLessOrEqual;
import static org.junit.jupiter.api.Assertions.*;

import com.kwezal.bearinmind.core.ControllerTestInterface;
import com.kwezal.bearinmind.core.course.dto.*;
import com.kwezal.bearinmind.core.course.model.Course;
import com.kwezal.bearinmind.core.course.model.CourseLesson;
import com.kwezal.bearinmind.core.course.repository.CourseLessonRepository;
import com.kwezal.bearinmind.core.exception.ErrorCode;
import com.kwezal.bearinmind.core.translation.model.Translation;
import com.kwezal.bearinmind.core.translation.repository.TranslationRepository;
import com.kwezal.bearinmind.core.utils.AuthHelper;
import com.kwezal.bearinmind.core.utils.TestConstants;
import com.kwezal.bearinmind.exception.response.ErrorResponse;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(
    scripts = { "/db/cleanup/COURSE_LESSON_PART.sql", "/db/cleanup/COURSE_LESSON.sql" },
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
class CourseLessonControllerTest implements ControllerTestInterface {

    private static final String LESSON_TOPIC_TRANSLATION_KEY = "topic";
    private static final String LESSON_DESCRIPTION_TRANSLATION_KEY = "description";

    @Override
    public String urlBase() {
        return "/course";
    }

    @Value("${application.locale}")
    private String applicationLocale;

    @Autowired
    private WebTestClient webClient;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CourseLessonRepository courseLessonRepository;

    @Autowired
    private TranslationRepository translationRepository;

    @Autowired
    private AuthHelper authHelper;

    @Transactional
    @ParameterizedTest
    @MethodSource("Should_CreateCourseLesson_When_CorrectRequest_Source")
    void Should_CreateCourseLesson_When_CorrectRequest(
        final int courseId,
        final OffsetDateTime lessonStartDateTime,
        final int expectedOrdinal
    ) {
        // GIVEN
        final var partLessonTranslations = Map.of(
            applicationLocale,
            "This is the content of a new lesson part",
            "pl",
            "To jest treść nowej części lekcji"
        );
        final var lessonPartDto = new CreateCourseLessonPartDto(
            partLessonTranslations,
            "StackOverFlow:https://stackoverflow.com/"
        );

        final var lessonTranslations = Map.of(
            applicationLocale,
            Map.of(
                LESSON_TOPIC_TRANSLATION_KEY,
                "This is the topic of a new lesson",
                LESSON_DESCRIPTION_TRANSLATION_KEY,
                "I will add description later."
            ),
            "pl",
            Map.of(
                LESSON_TOPIC_TRANSLATION_KEY,
                "To jest temat nowej lekcji",
                LESSON_DESCRIPTION_TRANSLATION_KEY,
                "Opis dodam później."
            )
        );
        final var dto = new CreateCourseLessonDto(lessonTranslations, lessonStartDateTime, List.of(lessonPartDto));

        // WHEN
        final var response = authHelper
            .asTeacher(
                webClient
                    .post()
                    .uri(builder -> url(builder, "/{courseId}/lesson").build(courseId))
                    .body(Mono.just(dto), CreateCourseLessonDto.class)
            )
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        //AND
        response
            .expectBody(Long.class)
            .value(id -> {
                assertNotNull(id);
                final var optionalNewCourseLesson = courseLessonRepository.findById(id);
                assertTrue(optionalNewCourseLesson.isPresent());
                final var newCourseLesson = optionalNewCourseLesson.get();

                assertTimeDifferenceLessOrEqual(lessonStartDateTime, newCourseLesson.getStartDateTime());
                assertNull(newCourseLesson.getImage());
                assertEquals(expectedOrdinal, newCourseLesson.getOrdinal());

                final var newCourseLessonPart = newCourseLesson.getParts().get(0);
                assertNotNull(newCourseLessonPart.getOrdinal());
                assertNotNull(newCourseLessonPart.getAttachments());
                assertNotNull(newCourseLessonPart.getTextIdentifier());
            });
    }

    private static Stream<Arguments> Should_CreateCourseLesson_When_CorrectRequest_Source() {
        return Stream.of(
            Arguments.of(3, OffsetDateTime.of(2020, 1, 3, 0, 0, 0, 0, ZoneOffset.UTC), 1),
            Arguments.of(2, OffsetDateTime.of(2020, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC), 4)
        );
    }

    @Test
    void Should_ReturnNotFound_When_AttemptToCreateCourseLessonForNonexistentCourse() {
        // GIVEN
        final var dto = new CreateCourseLessonDto(null, null, null);

        // WHEN
        final var response = authHelper
            .asTeacher(
                webClient
                    .post()
                    .uri(builder -> url(builder, "/{courseId}/lesson").build(TestConstants.NONEXISTENT_ID))
                    .body(Mono.just(dto), CreateCourseLessonDto.class)
            )
            .exchange();

        // THEN
        response.expectStatus().isNotFound();
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToCreateCourseLessonWithoutBody() {
        // GIVEN
        final var courseId = 1;

        // WHEN
        final var response = authHelper
            .asTeacher(webClient.post().uri(builder -> url(builder, "/{courseId}/lesson").build(courseId)))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnBadRequest_When_AttemptToCreateCourseLessonWithIncorrectLessonDate_Source")
    void Should_ReturnBadRequest_When_AttemptToCreateCourseLessonWithIncorrectLessonDate(
        OffsetDateTime lessonStartDateTime,
        String expectedErrorCode,
        Set<String> expectedArguments
    ) {
        // GIVEN
        final var courseId = 11;

        final var lessonTranslations = Map.of(
            applicationLocale,
            Map.of(
                LESSON_TOPIC_TRANSLATION_KEY,
                "This is the topic of a new lesson",
                LESSON_DESCRIPTION_TRANSLATION_KEY,
                "I will add description later."
            )
        );
        final var dto = new CreateCourseLessonDto(lessonTranslations, lessonStartDateTime, null);

        // WHEN
        final var response = authHelper
            .asTeacher(
                webClient
                    .post()
                    .uri(builder -> url(builder, "/{courseId}/lesson").build(courseId))
                    .body(Mono.just(dto), CreateCourseLessonDto.class)
            )
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();

        // AND
        response
            .expectBody(ErrorResponse.class)
            .value(responseDto -> {
                assertEquals(expectedErrorCode, responseDto.code());
                assertEqualsIgnoringOrder(expectedArguments, responseDto.arguments());
            });
    }

    private static Stream<Arguments> Should_ReturnBadRequest_When_AttemptToCreateCourseLessonWithIncorrectLessonDate_Source() {
        return Stream.of(
            Arguments.of(
                OffsetDateTime.of(2020, 1, 5, 9, 0, 0, 0, ZoneOffset.UTC),
                ErrorCode.INVALID_COURSE_LESSON_START_DATE_TIME_OR_END_DATE_TIME,
                Set.of("courseStartDateTime", "courseEndDateTime", "courseLesson")
            ),
            Arguments.of(
                OffsetDateTime.now().plusMonths(13),
                ErrorCode.INVALID_COURSE_LESSON_START_DATE_TIME_OR_END_DATE_TIME,
                Set.of("courseStartDateTime", "courseEndDateTime", "courseLesson")
            )
        );
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToCreateCourseWithIncorrectLessonPart() {
        // GIVEN
        final var courseId = 1;

        final var expectedArguments = Set.of("part");

        final var lessonPartDto = new CreateCourseLessonPartDto(null, null);

        final var lessonTranslations = Map.of(
            applicationLocale,
            Map.of(
                LESSON_TOPIC_TRANSLATION_KEY,
                "This is the topic of a new lesson",
                LESSON_DESCRIPTION_TRANSLATION_KEY,
                "I will add description later."
            )
        );
        final var lessonStartDateTime = OffsetDateTime.of(2020, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC);
        final var dto = new CreateCourseLessonDto(lessonTranslations, lessonStartDateTime, List.of(lessonPartDto));

        // WHEN
        final var response = authHelper
            .asTeacher(
                webClient
                    .post()
                    .uri(builder -> url(builder, "/{courseId}/lesson").build(courseId))
                    .body(Mono.just(dto), CreateCourseLessonDto.class)
            )
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();

        // AND
        response
            .expectBody(ErrorResponse.class)
            .value(responseDto -> {
                assertEquals(ErrorCode.INVALID_COURSE_LESSON_PART_ATTACHMENT_OR_TRANSLATIONS, responseDto.code());
                assertEqualsIgnoringOrder(expectedArguments, responseDto.arguments());
            });
    }

    @Test
    void Should_UpdateCourseLesson_When_CorrectRequest() {
        // GIVEN
        final var existingCourseLesson = createCourseLesson(1L, 1101, 3);

        final var lessonTranslations = Map.of(
            applicationLocale,
            Map.of(
                LESSON_TOPIC_TRANSLATION_KEY,
                "This is the topic of a new lesson",
                LESSON_DESCRIPTION_TRANSLATION_KEY,
                "I will add description later."
            ),
            "pl",
            Map.of(
                LESSON_TOPIC_TRANSLATION_KEY,
                "To jest temat nowej lekcji",
                LESSON_DESCRIPTION_TRANSLATION_KEY,
                "Opis dodam później."
            )
        );
        final var lessonStartDateTime = OffsetDateTime.of(2020, 1, 2, 0, 0, 0, 0, ZoneOffset.UTC);
        final var dto = new UpdateCourseLessonDto(lessonTranslations, lessonStartDateTime);

        // WHEN
        final var response = authHelper
            .asTeacher(
                webClient
                    .put()
                    .uri(builder -> url(builder, "/lesson/{id}").build(existingCourseLesson.getId()))
                    .body(Mono.just(dto), CreateCourseLessonDto.class)
            )
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        final var optionalEditedCourseLesson = courseLessonRepository.findById(existingCourseLesson.getId());

        assertTrue(optionalEditedCourseLesson.isPresent());
        // AND
        final var editedCourseLesson = optionalEditedCourseLesson.get();

        assertEquals(existingCourseLesson.getId(), editedCourseLesson.getId());

        assertTimeDifferenceLessOrEqual(lessonStartDateTime, editedCourseLesson.getStartDateTime());

        assertNull(editedCourseLesson.getImage());
        final var editedTranslations = translationRepository
            .findAllByIdentifierIn(
                Set.of(editedCourseLesson.getTopicIdentifier(), editedCourseLesson.getDescriptionIdentifier())
            )
            .stream()
            .map(Translation::getText)
            .collect(Collectors.toList());

        final var expectedTranslations = lessonTranslations
            .values()
            .stream()
            .flatMap(fieldTexts -> fieldTexts.values().stream())
            .collect(Collectors.toList());

        assertEqualsIgnoringOrder(expectedTranslations, editedTranslations);
    }

    @Test
    void Should_ReturnNotFound_When_AttemptToUpdateNonexistentCourseLesson() {
        // GIVEN
        final var lessonTranslations = Map.of(
            applicationLocale,
            Map.of(LESSON_TOPIC_TRANSLATION_KEY, "This is the topic of a new lesson")
        );
        final var dto = new UpdateCourseLessonDto(lessonTranslations, null);

        // WHEN
        final var response = authHelper
            .asTeacher(
                webClient
                    .put()
                    .uri(builder -> url(builder, "/lesson/{id}").build(TestConstants.NONEXISTENT_ID))
                    .body(Mono.just(dto), UpdateCourseLessonDto.class)
            )
            .exchange();

        // THEN
        response.expectStatus().isNotFound();
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToUpdateCourseLessonWithoutBody() {
        // GIVEN
        final var existingCourseLesson = createCourseLesson(1L, 2, 3);

        // WHEN
        final var response = authHelper
            .asTeacher(webClient.put().uri(builder -> url(builder, "/lesson/{id}").build(existingCourseLesson.getId())))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnBadRequest_When_AttemptToUpdateCourseLessonWithIncorrectDate_Source")
    void Should_ReturnBadRequest_When_AttemptToUpdateCourseLessonWithIncorrectDate(
        OffsetDateTime lessonStartDateTime,
        String expectedErrorCode,
        Set<String> expectedArguments
    ) {
        // GIVEN
        final var courseLessonId = 1L;

        final var lessonTranslations = Map.of(
            applicationLocale,
            Map.of(LESSON_TOPIC_TRANSLATION_KEY, "This is the topic of a new lesson")
        );
        final var dto = new UpdateCourseLessonDto(lessonTranslations, lessonStartDateTime);

        // WHEN
        final var response = authHelper
            .asTeacher(
                webClient
                    .put()
                    .uri(builder -> url(builder, "/lesson/{id}").build(courseLessonId))
                    .body(Mono.just(dto), UpdateCourseLessonDto.class)
            )
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();

        // AND
        response
            .expectBody(ErrorResponse.class)
            .value(responseDto -> {
                assertEquals(expectedErrorCode, responseDto.code());
                assertEqualsIgnoringOrder(expectedArguments, responseDto.arguments());
            });
    }

    private static Stream<Arguments> Should_ReturnBadRequest_When_AttemptToUpdateCourseLessonWithIncorrectDate_Source() {
        return Stream.of(
            Arguments.of(
                OffsetDateTime.of(2020, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC),
                ErrorCode.INVALID_COURSE_LESSON_START_DATE_TIME_OR_END_DATE_TIME,
                Set.of("courseStartDateTime", "courseEndDateTime", "courseLesson")
            ),
            Arguments.of(
                OffsetDateTime.of(2021, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC),
                ErrorCode.INVALID_COURSE_LESSON_START_DATE_TIME_OR_END_DATE_TIME,
                Set.of("courseStartDateTime", "courseEndDateTime", "courseLesson")
            )
        );
    }

    @Test
    void Should_ReturnCourseLesson_When_CourseLessonWithGivenIdExists() {
        // GIVEN
        final var id = 101;

        final var expectedTopic = "Hello Java World!";
        final var expectedDescription = "World and how to greet it";
        final var expectedParts = List.of(
            new CourseLessonPartDto(1, "<b><i>Hello</i></b> is a salutation or greeting in the English language.", null),
            new CourseLessonPartDto(2, null, "Source:https://en.wikipedia.org/wiki/Hello"),
            new CourseLessonPartDto(
                3,
                "It is first attested in writing from 1826.",
                ":https://www.oed.com/\\nModern English-Old High German dictionary:https://www.koeblergerhard.de/germanistischewoerterbuecher/althochdeutscheswoerterbuch/neuenglisch-ahd.pdf"
            )
        );

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/lesson/{id}").build(id)))
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(CourseLessonViewDto.class)
            .value(responseDto -> {
                assertNull(responseDto.image());
                assertEquals(expectedTopic, responseDto.topic());
                assertEquals(expectedDescription, responseDto.description());
                assertEquals(expectedParts, responseDto.parts());
            });
    }

    @Test
    void Should_ReturnForbidden_When_AttemptToGetCourseLessonWithoutHavingRoleInCourse() {
        // GIVEN
        final var id = 401;
        final var expectedErrorCode = "NO_ACCESS_TO_LESSON";

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/lesson/{id}").build(id)))
            .exchange();

        // THEN
        response.expectStatus().isForbidden();

        // AND
        response.expectBody(ErrorResponse.class).value(responseDto -> assertEquals(expectedErrorCode, responseDto.code()));
    }

    @Test
    void Should_ReturnForbidden_When_RequestedCourseLessonHasNotYetStarted() {
        // GIVEN
        final var id = 901;
        final var expectedErrorCode = "NO_ACCESS_TO_LESSON";

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/lesson/{id}").build(id)))
            .exchange();

        // THEN
        response.expectStatus().isForbidden();

        // AND
        response.expectBody(ErrorResponse.class).value(responseDto -> assertEquals(expectedErrorCode, responseDto.code()));
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToGetCourseLessonWithIncorrectId() {
        // GIVEN
        final var id = 0;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/lesson/{id}").build(id)))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @Test
    void Should_ReturnNotFound_When_RequestedCourseLessonDoesNotExist() {
        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/lesson/{id}").build(TestConstants.NONEXISTENT_ID)))
            .exchange();

        // THEN
        response.expectStatus().isNotFound();
    }

    private CourseLesson createCourseLesson(Long courseId, Integer topicIdentifier, Integer ordinal) {
        return createCourseLesson(courseId, topicIdentifier, null, ordinal, null);
    }

    private CourseLesson createCourseLesson(
        Long courseId,
        Integer topicIdentifier,
        Integer descriptionIdentifier,
        Integer ordinal,
        OffsetDateTime startDateTime
    ) {
        return courseLessonRepository.save(
            new CourseLesson(
                null,
                entityManager.getReference(Course.class, courseId),
                topicIdentifier,
                descriptionIdentifier,
                null,
                ordinal,
                startDateTime,
                List.of()
            )
        );
    }
}
