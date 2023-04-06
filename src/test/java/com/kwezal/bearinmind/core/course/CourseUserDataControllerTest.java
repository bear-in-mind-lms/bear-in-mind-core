package com.kwezal.bearinmind.core.course;

import static com.kwezal.bearinmind.core.exception.ErrorCode.CANNOT_ENROLL;
import static com.kwezal.bearinmind.core.exception.ErrorCode.FORBIDDEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kwezal.bearinmind.core.ControllerTestInterface;
import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import com.kwezal.bearinmind.core.course.repository.CourseUserDataRepository;
import com.kwezal.bearinmind.core.utils.AuthHelper;
import com.kwezal.bearinmind.exception.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/db/cleanup/COURSE_USER_DATA.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
class CourseUserDataControllerTest implements ControllerTestInterface {

    @Override
    public String urlBase() {
        return "/course";
    }

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private CourseUserDataRepository courseUserDataRepository;

    @Autowired
    private AuthHelper authHelper;

    @Test
    void Should_EnrollUserInCourse_When_CorrectRequest() {
        // GIVEN
        final var courseId = 11L;
        final var userId = 1L;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.post().uri(builder -> url(builder, "/enroll/{courseId}").build(courseId)))
            .exchange();

        // THEN
        response.expectStatus().isNoContent();

        // AND
        final var courseRole = courseUserDataRepository.findCourseRoleByCourseIdAndUserId(courseId, userId);

        assertTrue(courseRole.isPresent());
        assertEquals(CourseRole.STUDENT, courseRole.get());
    }

    @Test
    void Should_ReturnForbidden_When_AttemptToEnrollUserOutsideOfAuthorizedGroupInCourse() {
        // GIVEN
        final var courseId = 5;

        final var expectedErrorCode = FORBIDDEN;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.post().uri(builder -> url(builder, "/enroll/{courseId}").build(courseId)))
            .exchange();

        // THEN
        response.expectStatus().isForbidden();

        // AND
        response.expectBody(ErrorResponse.class).value(responseDto -> assertEquals(expectedErrorCode, responseDto.code()));
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToEnrollEnrolledUserInCourse() {
        // GIVEN
        final var courseId = 1;

        final var expectedErrorCode = CANNOT_ENROLL;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.post().uri(builder -> url(builder, "/enroll/{courseId}").build(courseId)))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();

        // AND
        response.expectBody(ErrorResponse.class).value(responseDto -> assertEquals(expectedErrorCode, responseDto.code()));
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToEnrollUserInCourseWithIncorrectCourseId() {
        // GIVEN
        final var courseId = 0;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.post().uri(builder -> url(builder, "/enroll/{courseId}").build(courseId)))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @Test
    void Should_ReturnCourseRole_When_UserHasRoleInCourse() {
        // GIVEN
        final var courseId = 2L;

        final var expectedRole = CourseRole.TEACHER;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/{courseId}/role").build(courseId)))
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response.expectBody(CourseRole.class).value(responseDto -> assertEquals(expectedRole, responseDto));
    }

    @Test
    void Should_ReturnNotFound_When_UserDoesNotHaveRoleInCourse() {
        // GIVEN
        final var courseId = 5L;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/{courseId}/role").build(courseId)))
            .exchange();

        // THEN
        response.expectStatus().isNotFound();
    }

    @Test
    void Should_ReturnCourseRole_When_UserHasRoleInCourseWithGivenLesson() {
        // GIVEN
        final var lessonId = 101L;

        final var expectedRole = CourseRole.TEACHER;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/lesson/{lessonId}/role").build(lessonId)))
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response.expectBody(CourseRole.class).value(responseDto -> assertEquals(expectedRole, responseDto));
    }

    @Test
    void Should_ReturnNotFound_When_UserDoesNotHaveRoleInCourseWithGivenLesson() {
        // GIVEN
        final var lessonId = 401L;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/lesson/{lessonId}/role").build(lessonId)))
            .exchange();

        // THEN
        response.expectStatus().isNotFound();
    }
}
