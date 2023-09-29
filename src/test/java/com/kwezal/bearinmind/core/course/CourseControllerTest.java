package com.kwezal.bearinmind.core.course;

import static com.kwezal.bearinmind.core.utils.AssertionUtils.assertEqualsIgnoringOrder;
import static com.kwezal.bearinmind.core.utils.AssertionUtils.assertTimeDifferenceLessOrEqual;
import static org.junit.jupiter.api.Assertions.*;

import com.kwezal.bearinmind.core.ControllerTestInterface;
import com.kwezal.bearinmind.core.course.dto.*;
import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import com.kwezal.bearinmind.core.course.model.Course;
import com.kwezal.bearinmind.core.course.model.CourseUserData;
import com.kwezal.bearinmind.core.course.repository.CourseRepository;
import com.kwezal.bearinmind.core.course.repository.CourseUserDataRepository;
import com.kwezal.bearinmind.core.exception.ErrorCode;
import com.kwezal.bearinmind.core.helper.Page;
import com.kwezal.bearinmind.core.user.dto.UserListItemDto;
import com.kwezal.bearinmind.core.user.model.User;
import com.kwezal.bearinmind.core.utils.AuthHelper;
import com.kwezal.bearinmind.core.utils.TestConstants;
import com.kwezal.bearinmind.exception.response.ErrorResponse;
import com.kwezal.bearinmind.translation.model.Translation;
import com.kwezal.bearinmind.translation.repository.TranslationRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
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
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(
    scripts = {
        "/db/cleanup/COURSE_USER_DATA.sql",
        "/db/cleanup/COURSE_LESSON_PART.sql",
        "/db/cleanup/COURSE_LESSON.sql",
        "/db/cleanup/COURSE.sql",
    },
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
class CourseControllerTest implements ControllerTestInterface {

    private static final String COURSE_NAME_TRANSLATION_KEY = "name";
    private static final String COURSE_DESCRIPTION_TRANSLATION_KEY = "description";

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

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TranslationRepository translationRepository;

    @Autowired
    private AuthHelper authHelper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private CourseUserDataRepository courseUserDataRepository;

    @Transactional
    @Test
    void Should_CreateCourseWithoutLessons_When_CorrectRequest() {
        // GIVEN
        final var translations = Map.of(
            applicationLocale,
            Map.of(
                COURSE_NAME_TRANSLATION_KEY,
                "This is the name of a new course",
                COURSE_DESCRIPTION_TRANSLATION_KEY,
                "I will add description later."
            ),
            "pl",
            Map.of(
                COURSE_NAME_TRANSLATION_KEY,
                "To jest tytuł nowego kursu",
                COURSE_DESCRIPTION_TRANSLATION_KEY,
                "Opis dodam później."
            )
        );
        final var startDateTime = OffsetDateTime.of(2021, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC);
        final var endDateTime = OffsetDateTime.now().plusMonths(12);
        final var dto = new CreateCourseDto(translations, startDateTime, endDateTime, null, null);

        // WHEN
        final var response = authHelper
            .asTeacher(webClient.post().uri(url()).body(Mono.just(dto), CreateCourseDto.class))
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(Long.class)
            .value(id -> {
                assertNotNull(id);
                final var optionalNewCourse = courseRepository.findById(id);
                assertTrue(optionalNewCourse.isPresent());
                final var newCourse = optionalNewCourse.get();
                assertNotNull(newCourse.getNameIdentifier());
                assertNotNull(newCourse.getDescriptionIdentifier());
                assertNull(newCourse.getImage());
                assertTimeDifferenceLessOrEqual(startDateTime, newCourse.getStartDateTime());
                assertTimeDifferenceLessOrEqual(endDateTime, newCourse.getEndDateTime());
                assertNotNull(newCourse.getCreationDateTime());
                assertNull(newCourse.getRegistrationClosingDateTime());
                assertTrue(newCourse.getLessons().isEmpty());
                assertFalse(newCourse.getData().isEmpty());
                assertEquals(AuthHelper.DEFAULT_USER_ID, newCourse.getData().get(0).getUser().getId());
            });
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnBadRequest_When_AttemptToCreateCourseWithIncorrectDates_Source")
    void Should_ReturnBadRequest_When_AttemptToCreateCourseWithIncorrectDates(
        OffsetDateTime startDateTime,
        OffsetDateTime endDateTime,
        OffsetDateTime registrationClosingDateTime,
        String expectedErrorCode,
        Set<String> expectedArguments
    ) {
        // GIVEN
        final var translations = Map.of(
            applicationLocale,
            Map.of(COURSE_NAME_TRANSLATION_KEY, "This is the name of a new course")
        );
        final var dto = new CreateCourseDto(translations, startDateTime, endDateTime, registrationClosingDateTime, null);

        // WHEN
        final var response = authHelper
            .asTeacher(webClient.post().uri(url()).body(Mono.just(dto), CreateCourseDto.class))
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

    private static Stream<Arguments> Should_ReturnBadRequest_When_AttemptToCreateCourseWithIncorrectDates_Source() {
        return Stream.of(
            Arguments.of(
                OffsetDateTime.now().plusMonths(2),
                OffsetDateTime.now().plusMonths(1),
                null,
                ErrorCode.INVALID_COURSE_START_DATE_TIME_IS_AFTER_END_DATE_TIME,
                Set.of("startDateTime", "endDateTime")
            ),
            Arguments.of(
                null,
                OffsetDateTime.now().plusMonths(1),
                OffsetDateTime.now().plusMonths(2),
                ErrorCode.INVALID_COURSE_REGISTRATION_CLOSING_DATE_TIME_IS_AFTER_END_DATE_TIME,
                Set.of("registrationClosingDateTime", "endDateTime")
            ),
            Arguments.of(
                null,
                OffsetDateTime.now().minusMonths(1),
                null,
                ErrorCode.INVALID_COURSE_END_DATE_TIME_IS_BEFORE_NOW_TIME,
                Set.of("endDateTime")
            ),
            Arguments.of(
                OffsetDateTime.now().plusMonths(2),
                null,
                OffsetDateTime.now().plusMonths(1),
                ErrorCode.INVALID_COURSE_START_DATE_TIME_IS_AFTER_REGISTRATION_CLOSING_DATE_TIME,
                Set.of("startDateTime", "registrationClosingDateTime")
            ),
            Arguments.of(
                null,
                null,
                OffsetDateTime.now().minusMonths(1),
                ErrorCode.INVALID_COURSE_REGISTRATION_CLOSING_DATE_TIME_IS_BEFORE_NOW_TIME,
                Set.of("registrationClosingDateTime")
            )
        );
    }

    @Transactional
    @Test
    void Should_CreateCourse_When_CorrectRequest() {
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
        final var lessonStartDateTime = OffsetDateTime.of(2021, 1, 1, 9, 0, 0, 0, ZoneOffset.UTC);
        final var lessonDto = new CreateCourseLessonDto(lessonTranslations, lessonStartDateTime, List.of(lessonPartDto));

        final var translations = Map.of(
            applicationLocale,
            Map.of(
                COURSE_NAME_TRANSLATION_KEY,
                "This is the name of a new course",
                COURSE_DESCRIPTION_TRANSLATION_KEY,
                "I will add description later."
            ),
            "pl",
            Map.of(
                COURSE_NAME_TRANSLATION_KEY,
                "To jest tytuł nowego kursu",
                COURSE_DESCRIPTION_TRANSLATION_KEY,
                "Opis dodam później."
            )
        );
        final var startDateTime = OffsetDateTime.of(2021, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC);
        final var endDateTime = OffsetDateTime.now().plusMonths(12);
        final var dto = new CreateCourseDto(translations, startDateTime, endDateTime, null, List.of(lessonDto));

        // WHEN
        final var response = authHelper
            .asTeacher(webClient.post().uri(url()).body(Mono.just(dto), CreateCourseDto.class))
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(Long.class)
            .value(id -> {
                assertNotNull(id);
                final var optionalNewCourse = courseRepository.findById(id);
                assertTrue(optionalNewCourse.isPresent());
                final var newCourse = optionalNewCourse.get();
                assertNotNull(newCourse.getNameIdentifier());
                assertNotNull(newCourse.getDescriptionIdentifier());
                assertNull(newCourse.getImage());
                assertTimeDifferenceLessOrEqual(startDateTime, newCourse.getStartDateTime());
                assertTimeDifferenceLessOrEqual(endDateTime, newCourse.getEndDateTime());
                assertNotNull(newCourse.getCreationDateTime());
                assertNull(newCourse.getRegistrationClosingDateTime());
                assertFalse(newCourse.getLessons().isEmpty());

                final var newCourseLesson = newCourse.getLessons().get(0);
                assertNull(newCourseLesson.getImage());
                assertTimeDifferenceLessOrEqual(lessonStartDateTime, newCourseLesson.getStartDateTime());
                assertNotNull(newCourseLesson.getOrdinal());

                final var newCourseLessonPart = newCourseLesson.getParts().get(0);
                assertNotNull(newCourseLessonPart.getOrdinal());
                assertNotNull(newCourseLessonPart.getAttachments());
                assertNotNull(newCourseLessonPart.getTextIdentifier());

                assertFalse(newCourse.getData().isEmpty());
                assertEquals(AuthHelper.DEFAULT_USER_ID, newCourse.getData().get(0).getUser().getId());
            });
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnBadRequest_When_AttemptToCreateCourseWithIncorrectLessonDate_Source")
    void Should_ReturnBadRequest_When_AttemptToCreateCourseWithIncorrectLessonDate(
        OffsetDateTime lessonStartDateTime,
        String expectedErrorCode,
        Set<String> expectedArguments
    ) {
        // GIVEN

        final var partLessonTranslations = Map.of(applicationLocale, "This is the content of a new lesson part");
        final var lessonPartDto = new CreateCourseLessonPartDto(
            partLessonTranslations,
            "StackOverFlow:https://stackoverflow.com/"
        );

        final var lessonTranslations = Map.of(
            applicationLocale,
            Map.of(LESSON_TOPIC_TRANSLATION_KEY, "This is the topic of a new lesson")
        );
        final var lessonDto = new CreateCourseLessonDto(lessonTranslations, lessonStartDateTime, List.of(lessonPartDto));

        final var translations = Map.of(
            applicationLocale,
            Map.of(COURSE_NAME_TRANSLATION_KEY, "This is the name of a new course")
        );
        final var startDateTime = OffsetDateTime.of(2021, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC);
        final var endDateTime = OffsetDateTime.now().plusMonths(12);
        final var dto = new CreateCourseDto(translations, startDateTime, endDateTime, null, List.of(lessonDto));

        // WHEN
        final var response = authHelper
            .asTeacher(webClient.post().uri(url()).body(Mono.just(dto), CreateCourseDto.class))
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

    private static Stream<Arguments> Should_ReturnBadRequest_When_AttemptToCreateCourseWithIncorrectLessonDate_Source() {
        return Stream.of(
            Arguments.of(
                OffsetDateTime.of(2021, 1, 1, 7, 0, 0, 0, ZoneOffset.UTC),
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
        final var expectedArguments = Set.of("part");

        final var lessonPartDto = new CreateCourseLessonPartDto(null, null);

        final var lessonTranslations = Map.of(
            applicationLocale,
            Map.of(LESSON_TOPIC_TRANSLATION_KEY, "This is the topic of a new lesson")
        );
        final var lessonDto = new CreateCourseLessonDto(lessonTranslations, null, List.of(lessonPartDto));

        final var translations = Map.of(
            applicationLocale,
            Map.of(COURSE_NAME_TRANSLATION_KEY, "This is the name of a new course")
        );
        final var dto = new CreateCourseDto(translations, null, null, null, List.of(lessonDto));

        // WHEN
        final var response = authHelper
            .asTeacher(webClient.post().uri(url()).body(Mono.just(dto), CreateCourseDto.class))
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
    void Should_ReturnBadRequest_When_AttemptToCreateCourseWithoutBody() {
        // WHEN
        final var response = authHelper.asTeacher(webClient.post().uri(url())).exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @Test
    void Should_UpdateCourse_When_CorrectRequest() {
        // GIVEN
        final var existingCourse = createCourse(1);
        final var existingCourseId = existingCourse.getId();
        createCourseUserData(existingCourseId, AuthHelper.DEFAULT_USER_ID, CourseRole.OWNER);

        final var courseTranslations = Map.of(
            applicationLocale,
            Map.of(
                COURSE_NAME_TRANSLATION_KEY,
                "This is the name of a edited course",
                COURSE_DESCRIPTION_TRANSLATION_KEY,
                "I could add longer description but I don't a time."
            ),
            "pl",
            Map.of(
                COURSE_NAME_TRANSLATION_KEY,
                "To jest tytuł zaktualizowanego kursu",
                COURSE_DESCRIPTION_TRANSLATION_KEY,
                "Mógłbym dodać dłuższy opis, ale nie mam czasu."
            )
        );
        final var startDateTime = OffsetDateTime.of(2021, 1, 1, 8, 0, 0, 0, ZoneOffset.UTC);
        final var endDateTime = OffsetDateTime.now().plusMonths(12);
        final var dto = new UpdateCourseDto(courseTranslations, startDateTime, endDateTime, null);

        // WHEN
        final var response = authHelper
            .asTeacher(
                webClient
                    .put()
                    .uri(builder -> url(builder, "/{id}").build(existingCourseId))
                    .body(Mono.just(dto), UpdateCourseDto.class)
            )
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        final var optionalEditedCourse = courseRepository.findById(existingCourseId);
        assertTrue(optionalEditedCourse.isPresent());

        // AND
        final var editedCourse = optionalEditedCourse.get();

        assertEquals(existingCourseId, editedCourse.getId());

        assertNotNull(editedCourse.getNameIdentifier());
        assertNotNull(editedCourse.getDescriptionIdentifier());
        assertNull(editedCourse.getImage());
        assertTimeDifferenceLessOrEqual(startDateTime, editedCourse.getStartDateTime());
        assertTimeDifferenceLessOrEqual(endDateTime, editedCourse.getEndDateTime());
        assertNotNull(editedCourse.getCreationDateTime());
        assertNull(editedCourse.getRegistrationClosingDateTime());

        final var editedTranslations = translationRepository
            .findAllByIdentifierIn(Set.of(editedCourse.getNameIdentifier(), editedCourse.getDescriptionIdentifier()))
            .stream()
            .map(Translation::getText)
            .collect(Collectors.toList());

        final var expectedTranslations = courseTranslations
            .values()
            .stream()
            .flatMap(fieldTexts -> fieldTexts.values().stream())
            .collect(Collectors.toList());
        assertEqualsIgnoringOrder(expectedTranslations, editedTranslations);
    }

    @Test
    void Should_ReturnNotFound_When_AttemptToUpdateNonexistentCourse() {
        // GIVEN
        final var dto = new UpdateCourseDto(Collections.emptyMap(), null, null, null);

        // WHEN
        final var response = authHelper
            .asTeacher(
                webClient
                    .put()
                    .uri(builder -> url(builder, "/{id}").build(TestConstants.NONEXISTENT_ID))
                    .body(Mono.just(dto), UpdateCourseDto.class)
            )
            .exchange();

        // THEN
        response.expectStatus().isNotFound();
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToUpdateCourseWithoutBody() {
        // GIVEN
        final var existingCourse = createCourse(1);

        // WHEN
        final var response = authHelper
            .asTeacher(webClient.put().uri(builder -> url(builder, "/{id}").build(existingCourse.getId())))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnCoursesMainView_When_CorrectRequest_Source")
    void Should_ReturnCoursesMainView_When_CorrectRequest(
        int listLengthParam,
        List<Long> expectedIdsOfActiveListParam,
        List<Long> expectedIdsOfAvailableListParam,
        List<Long> expectedIdsOfCompletedListParam
    ) {
        // GIVEN
        final var listLength = listLengthParam;
        final var expectedIdsOfActiveList = expectedIdsOfActiveListParam;
        final var expectedIdsOfAvailableList = expectedIdsOfAvailableListParam;
        final var expectedIdsOfCompletedList = expectedIdsOfCompletedListParam;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/main-view").queryParam("listLength", listLength).build()))
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(CourseMainViewDto.class)
            .value(responseDto -> {
                assertEquals(expectedIdsOfActiveList, responseDto.activeCourses().stream().map(CourseListItemDto::id).toList());
                assertEquals(
                    expectedIdsOfAvailableList,
                    responseDto.availableCourses().stream().map(CourseListItemDto::id).toList()
                );
                assertEquals(
                    expectedIdsOfCompletedList,
                    responseDto.completedCourses().stream().map(CourseListItemDto::id).toList()
                );
            });
    }

    private static Stream<Arguments> Should_ReturnCoursesMainView_When_CorrectRequest_Source() {
        return Stream.of(
            Arguments.of(10, List.of(10L, 8L, 4L, 3L, 2L), List.of(11L), List.of(9L, 1L)),
            Arguments.of(3, List.of(10L, 8L, 4L), List.of(11L), List.of(9L, 1L))
        );
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 11 })
    void Should_ReturnBadRequest_When_AttemptToGetMainViewCoursesWithIncorrectLength(int paramListLength) {
        // GIVEN
        final var listLength = paramListLength;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/main-view").queryParam("listLength", listLength).build()))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @Test
    void Should_ReturnActiveCourses_When_CorrectRequest() {
        // GIVEN
        final var pageNumber = 0;
        final var pageSize = 10;
        final var expectedCourseIds = List.of(10L, 8L, 4L, 3L, 2L);

        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .get()
                    .uri(builder ->
                        url(builder, "/list/active")
                            .queryParam("pageNumber", pageNumber)
                            .queryParam("pageSize", pageSize)
                            .build()
                    )
            )
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(new ParameterizedTypeReference<Page<CourseListItemDto>>() {})
            .value(responseDto -> {
                assertEquals(pageNumber, responseDto.getNumber());
                assertEquals(expectedCourseIds, responseDto.getContent().stream().map(CourseListItemDto::id).toList());
            });
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnBadRequest_When_AttemptToGetActiveCoursesWithIncorrectData_Source")
    void Should_ReturnBadRequest_When_AttemptToGetActiveCoursesWithIncorrectData(int pageNumber, int pageSize) {
        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .get()
                    .uri(builder ->
                        url(builder, "/list/active")
                            .queryParam("pageNumber", pageNumber)
                            .queryParam("pageSize", pageSize)
                            .build()
                    )
            )
            .exchange();
        // THEN
        response.expectStatus().isBadRequest();
    }

    private static Stream<Arguments> Should_ReturnBadRequest_When_AttemptToGetActiveCoursesWithIncorrectData_Source() {
        return Stream.of(Arguments.of(-1, 1), Arguments.of(1, 0), Arguments.of(1, 101));
    }

    @Test
    void Should_ReturnAvailableCourses_When_CorrectRequest() {
        // GIVEN
        final var pageNumber = 0;
        final var pageSize = 10;
        final var expectedCourseIds = List.of(11L);

        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .get()
                    .uri(builder ->
                        url(builder, "/list/available")
                            .queryParam("pageNumber", pageNumber)
                            .queryParam("pageSize", pageSize)
                            .build()
                    )
            )
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(new ParameterizedTypeReference<Page<CourseListItemDto>>() {})
            .value(responseDto -> {
                assertEquals(pageNumber, responseDto.getNumber());
                assertEqualsIgnoringOrder(
                    expectedCourseIds,
                    responseDto.getContent().stream().map(CourseListItemDto::id).toList()
                );
            });
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnBadRequest_When_AttemptToGetAvailableCoursesWithIncorrectData_Source")
    void Should_ReturnBadRequest_When_AttemptToGetAvailableCoursesWithIncorrectData(int pageNumber, int pageSize) {
        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .get()
                    .uri(builder ->
                        url(builder, "/list/active")
                            .queryParam("pageNumber", pageNumber)
                            .queryParam("pageSize", pageSize)
                            .build()
                    )
            )
            .exchange();
        // THEN
        response.expectStatus().isBadRequest();
    }

    private static Stream<Arguments> Should_ReturnBadRequest_When_AttemptToGetAvailableCoursesWithIncorrectData_Source() {
        return Stream.of(Arguments.of(-2, 2), Arguments.of(2, -1), Arguments.of(2, 200));
    }

    @Test
    void Should_ReturnCompletedCourses_When_CorrectRequest() {
        // GIVEN
        final var pageNumber = 0;
        final var pageSize = 10;
        final var expectedCourseIds = List.of(9L, 1L);

        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .get()
                    .uri(builder ->
                        url(builder, "/list/completed")
                            .queryParam("pageNumber", pageNumber)
                            .queryParam("pageSize", pageSize)
                            .build()
                    )
            )
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(new ParameterizedTypeReference<Page<CourseListItemDto>>() {})
            .value(responseDto -> {
                assertEquals(pageNumber, responseDto.getNumber());
                assertEquals(expectedCourseIds, responseDto.getContent().stream().map(CourseListItemDto::id).toList());
            });
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnBadRequest_When_AttemptToGetCompletedCoursesWithIncorrectData_Source")
    void Should_ReturnBadRequest_When_AttemptToGetCompletedCoursesWithIncorrectData(int pageNumber, int pageSize) {
        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .get()
                    .uri(builder ->
                        url(builder, "/list/active")
                            .queryParam("pageNumber", pageNumber)
                            .queryParam("pageSize", pageSize)
                            .build()
                    )
            )
            .exchange();
        // THEN
        response.expectStatus().isBadRequest();
    }

    private static Stream<Arguments> Should_ReturnBadRequest_When_AttemptToGetCompletedCoursesWithIncorrectData_Source() {
        return Stream.of(Arguments.of(-3, 3), Arguments.of(3, -3), Arguments.of(3, 300));
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnActiveCoursePage_When_CorrectRequest_Source")
    void Should_ReturnActiveCoursePage_When_CorrectRequest(
        long courseId,
        String expectedName,
        String expectedDescription,
        List<Long> expectedLessonIds,
        List<Long> expectedTeacherIds,
        OffsetDateTime expectedEndDateTime
    ) {
        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/{courseId}").build(courseId)))
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(CourseViewDto.class)
            .value(responseDto -> {
                assertEquals(expectedName, responseDto.name());
                assertEquals(expectedDescription, responseDto.description());
                assertEqualsIgnoringOrder(
                    expectedLessonIds,
                    responseDto.lessons().stream().map(CourseLessonCardDto::id).toList()
                );
                assertEqualsIgnoringOrder(
                    expectedTeacherIds,
                    responseDto.teachers().stream().map(UserListItemDto::id).toList()
                );
                assertTimeDifferenceLessOrEqual(expectedEndDateTime, responseDto.endDateTime());
                assertNotNull(responseDto.active());
                assertNull(responseDto.available());
                assertNull(responseDto.completed());
            });
    }

    private static Stream<Arguments> Should_ReturnActiveCoursePage_When_CorrectRequest_Source() {
        return Stream.of(
            Arguments.of(2L, "Java", "Discover the finest Indonesian coffee", List.of(101L, 102L, 103L), List.of(1L, 2L), null),
            Arguments.of(
                8L,
                "Docker",
                "Put the container on the whale's back",
                List.of(),
                List.of(4L),
                OffsetDateTime.now().plusDays(2013)
            )
        );
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnAvailableCoursePage_When_CorrectRequest_Source")
    void Should_ReturnAvailableCoursePage_When_CorrectRequest(
        Long courseId,
        String expectedName,
        String expectedDescription,
        List<Long> expectedTeacherIds,
        List<Long> expectedLessonIds,
        OffsetDateTime expectedEndDateTime,
        OffsetDateTime expectedRegistrationClosingDateTime
    ) {
        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/{courseId}").build(courseId)))
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(CourseViewDto.class)
            .value(responseDto -> {
                assertEquals(expectedName, responseDto.name());
                assertEquals(expectedDescription, responseDto.description());
                assertEqualsIgnoringOrder(
                    expectedTeacherIds,
                    responseDto.teachers().stream().map(UserListItemDto::id).toList()
                );
                assertEqualsIgnoringOrder(
                    expectedLessonIds,
                    responseDto.lessons().stream().map(CourseLessonCardDto::id).toList()
                );
                assertTimeDifferenceLessOrEqual(expectedEndDateTime, responseDto.endDateTime());
                assertTimeDifferenceLessOrEqual(
                    expectedRegistrationClosingDateTime,
                    responseDto.available().registrationClosingDateTime()
                );
                assertNull(responseDto.active());
                assertNotNull(responseDto.available());
                assertNull(responseDto.completed());
            });
    }

    private static Stream<Arguments> Should_ReturnAvailableCoursePage_When_CorrectRequest_Source() {
        return Stream.of(
            Arguments.of(
                11L,
                "Engelsk",
                null,
                List.of(5L),
                List.of(),
                OffsetDateTime.now().plusDays(365),
                OffsetDateTime.now().plusDays(7)
            )
        );
    }

    @Test
    void Should_ReturnCompletedCoursePage_When_CorrectRequest() {
        // GIVEN
        final var courseId = 9L;
        final var expectedTeacherIds = List.of(4L);
        final var expectedName = "Kubernetes";
        final var expectedDescription = "/ˌk(j)uːbərˈnɛtɪs, -ˈneɪtɪs, -ˈneɪtiːz, -ˈnɛtiːz/";
        final var expectedEndDateTime = OffsetDateTime.of(2021, 1, 4, 10, 0, 0, 0, ZoneOffset.UTC);

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/{courseId}").build(courseId)))
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(CourseViewDto.class)
            .value(responseDto -> {
                assertEquals(expectedName, responseDto.name());
                assertEquals(expectedDescription, responseDto.description());
                assertTrue(responseDto.lessons().isEmpty());
                assertEqualsIgnoringOrder(
                    expectedTeacherIds,
                    responseDto.teachers().stream().map(UserListItemDto::id).toList()
                );
                assertEquals(expectedEndDateTime, responseDto.endDateTime());
                assertNull(responseDto.active());
                assertNull(responseDto.available());
                assertNotNull(responseDto.completed());
            });
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToGetCompletedCoursesWithIncorrectData() {
        // GIVEN
        final var courseId = 0L;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/{courseId}").build(courseId)))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    private Course createCourse(Integer nameIdentifier) {
        return createCourse(nameIdentifier, null, null, null, OffsetDateTime.now(), null);
    }

    private Course createCourse(
        Integer nameIdentifier,
        Integer descriptionIdentifier,
        OffsetDateTime startDateTime,
        OffsetDateTime endDateTime,
        OffsetDateTime creationDateTime,
        OffsetDateTime registrationClosingDateTime
    ) {
        return courseRepository.save(
            new Course(
                null,
                nameIdentifier,
                descriptionIdentifier,
                null,
                startDateTime,
                endDateTime,
                creationDateTime,
                registrationClosingDateTime,
                null,
                null
            )
        );
    }

    private void createCourseUserData(Long courseId, Long userId, CourseRole role) {
        courseUserDataRepository.save(
            new CourseUserData(
                null,
                entityManager.getReference(Course.class, courseId),
                entityManager.getReference(User.class, userId),
                role,
                OffsetDateTime.now()
            )
        );
    }
}
