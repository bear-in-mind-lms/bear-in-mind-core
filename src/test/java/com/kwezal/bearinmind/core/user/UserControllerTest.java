package com.kwezal.bearinmind.core.user;

import static com.kwezal.bearinmind.core.utils.AssertionUtils.assertEqualsIgnoringOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kwezal.bearinmind.core.ControllerTestInterface;
import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import com.kwezal.bearinmind.core.user.dto.*;
import com.kwezal.bearinmind.core.user.model.User;
import com.kwezal.bearinmind.core.user.model.UserCredentials;
import com.kwezal.bearinmind.core.user.repository.UserCredentialsRepository;
import com.kwezal.bearinmind.core.user.repository.UserRepository;
import com.kwezal.bearinmind.core.utils.AuthHelper;
import com.kwezal.bearinmind.core.utils.Page;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(
    scripts = { "/db/cleanup/USER.sql", "/db/cleanup/USER_CREDENTIALS.sql" },
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
class UserControllerTest implements ControllerTestInterface {

    @Override
    public String urlBase() {
        return "/user";
    }

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private AuthHelper authHelper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    @Test
    void Should_UpdateUser_When_CorrectRequest() {
        // GIVEN
        final var user = createUser();
        final var userId = user.getId();
        final var username = user.getUserCredentials().getUsername();
        final var locale = user.getLocale();

        final var expectedFirstName = "Tobias";
        final var expectedMiddleName = "Hendrik";
        final var expectedLastName = "Rieper";
        final var expectedTitle = "Agent";
        final var expectedLocale = "en";
        final var expectedPhoneNumber = "777777777";
        final var dto = new UpdateUserDto(
            expectedFirstName,
            expectedMiddleName,
            expectedLastName,
            expectedTitle,
            expectedLocale,
            expectedPhoneNumber
        );

        // WHEN
        final var response = authHelper
            .asUserWithRoleAndId(
                UserRole.STUDENT,
                userId,
                username,
                locale,
                webClient.put().uri(url()).body(Mono.just(dto), UpdateUserDto.class)
            )
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        final var optionalUpdatedUser = userRepository.findById(userId);
        assertTrue(optionalUpdatedUser.isPresent());
        final var updatedUser = optionalUpdatedUser.get();

        assertEquals(expectedFirstName, updatedUser.getFirstName());
        assertEquals(expectedMiddleName, updatedUser.getMiddleName());
        assertEquals(expectedLastName, updatedUser.getLastName());
        assertEquals(expectedTitle, updatedUser.getTitle());
        assertEquals(expectedLocale, updatedUser.getLocale());
        assertEquals(expectedPhoneNumber, updatedUser.getPhoneNumber());
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToUpdateUserWithoutBody() {
        // WHEN
        final var response = authHelper.asStudent(webClient.put().uri(url())).exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    private record CourseIdCourseRole(Long id, CourseRole role) {}

    @Test
    void Should_ReturnUserPage_When_CorrectRequest() {
        // GIVEN
        final var userId = 2L;
        final var expectedName = "James Gosling";
        final var expectedTitle = "PhD";
        final var expectedRegistrationDateTime = OffsetDateTime.of(2020, 1, 2, 8, 0, 0, 0, ZoneOffset.UTC);
        final var expectedCourses = List.of(
            new CourseIdCourseRole(2L, CourseRole.OWNER),
            new CourseIdCourseRole(3L, CourseRole.OWNER),
            new CourseIdCourseRole(4L, CourseRole.OWNER),
            new CourseIdCourseRole(10L, CourseRole.STUDENT)
        );
        final var expectedGroupIds = List.of(2L);

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/{id}").build(userId)))
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(UserViewDto.class)
            .value(responseDto -> {
                assertEquals(expectedName, responseDto.name());
                assertEquals(expectedTitle, responseDto.title());
                assertEquals(expectedRegistrationDateTime, responseDto.registrationDateTime());
                assertEqualsIgnoringOrder(
                    expectedCourses,
                    responseDto.courses().stream().map(course -> new CourseIdCourseRole(course.id(), course.role())).toList()
                );
                assertEqualsIgnoringOrder(
                    expectedGroupIds,
                    responseDto.groups().stream().map(UserGroupListItemDto::id).toList()
                );
            });
    }

    @Test
    void Should_ReturnNotFound_When_RequestedUserDoesNotExist() {
        // GIVEN
        final var userId = 1000L;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/{id}").build(userId)))
            .exchange();

        // THEN
        response.expectStatus().isNotFound();
    }

    @Test
    void Should_ReturnUsersMainView_When_CorrectRequest() {
        // GIVEN
        final var listLength = 3;
        final var expectedRegisteredGroupIds = List.of(2L, 1L);
        final var expectedAvailableGroupIds = List.of(3L);

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/main-view").queryParam("listLength", listLength).build()))
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(UserMainViewDto.class)
            .value(responseDto -> {
                assertEquals(
                    expectedRegisteredGroupIds,
                    responseDto.registeredGroups().stream().map(UserGroupListItemDto::id).toList()
                );
                assertEquals(
                    expectedAvailableGroupIds,
                    responseDto.availableGroups().stream().map(UserGroupListItemDto::id).toList()
                );
                assertTrue(responseDto.hasTeachers());
                assertTrue(responseDto.hasStudents());
            });
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 11 })
    void Should_ReturnBadRequest_When_AttemptToGetUsersMainViewWithIncorrectLength(int listLength) {
        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/main-view").queryParam("listLength", listLength).build()))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @Test
    void Should_ReturnGroupMembers_When_CorrectRequest() {
        // GIVEN
        final var pageNumber = 0;
        final var pageSize = 5;
        final var expectedUserIds = List.of(2L, 3L, 4L, 5L);

        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .get()
                    .uri(builder ->
                        url(builder, "/list/group-members")
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
            .expectBody(new ParameterizedTypeReference<Page<UserListItemDto>>() {})
            .value(responseDto -> {
                assertEquals(pageNumber, responseDto.getNumber());
                assertEqualsIgnoringOrder(expectedUserIds, responseDto.getContent().stream().map(UserListItemDto::id).toList());
            });
    }

    @Test
    void Should_ReturnStudents_When_CorrectRequest() {
        // GIVEN
        final var pageNumber = 0;
        final var pageSize = 10;
        final var expectedUserIds = List.of(6L, 7L, 8L, 9L, 10L);

        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .get()
                    .uri(builder ->
                        url(builder, "/list/students")
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
            .expectBody(new ParameterizedTypeReference<Page<UserListItemDto>>() {})
            .value(responseDto -> {
                assertEquals(pageNumber, responseDto.getNumber());
                assertEqualsIgnoringOrder(expectedUserIds, responseDto.getContent().stream().map(UserListItemDto::id).toList());
            });
    }

    @Test
    void Should_ReturnTeachers_When_CorrectRequest() {
        // GIVEN
        final var pageNumber = 0;
        final var pageSize = 10;
        final var expectedUserIds = List.of(4L, 5L);

        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .get()
                    .uri(builder ->
                        url(builder, "/list/teachers")
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
            .expectBody(new ParameterizedTypeReference<Page<UserListItemDto>>() {})
            .value(responseDto -> {
                assertEquals(pageNumber, responseDto.getNumber());
                assertEqualsIgnoringOrder(expectedUserIds, responseDto.getContent().stream().map(UserListItemDto::id).toList());
            });
    }

    private User createUser() {
        final var userCredentials = userCredentialsRepository.save(
            new UserCredentials(
                null,
                "test",
                "$2a$04$PTBFP2iKcm1CXmjn6uMFk.IbgAPisD3MH.fGvhBN40vF97RuwVYVm",
                UserRole.ADMINISTRATOR_ROLE_GROUP,
                true
            )
        );
        return userRepository.save(
            new User(
                null,
                userCredentials,
                "Test",
                null,
                "Testson",
                null,
                "test@bearinmind.kwezal.com",
                null,
                "en",
                null,
                OffsetDateTime.now()
            )
        );
    }
}
