package com.kwezal.bearinmind.core.auth;

import static com.kwezal.bearinmind.core.utils.AssertionUtils.assertEqualsIgnoringOrder;
import static com.kwezal.bearinmind.core.utils.TestConstants.ID_SEQUENCE_START;
import static org.junit.jupiter.api.Assertions.*;

import com.kwezal.bearinmind.core.ControllerTest;
import com.kwezal.bearinmind.core.auth.dto.CredentialsDto;
import com.kwezal.bearinmind.core.auth.dto.LoginResponseDto;
import com.kwezal.bearinmind.core.auth.enumeration.AuthClient;
import com.kwezal.bearinmind.core.auth.service.AuthJwtService;
import com.kwezal.bearinmind.core.exception.ErrorCode;
import com.kwezal.bearinmind.core.user.dto.CreateUserDto;
import com.kwezal.bearinmind.core.user.dto.UserRole;
import com.kwezal.bearinmind.exception.response.ErrorResponse;
import java.time.Duration;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(
    scripts = { "/db/cleanup/USER.sql", "/db/cleanup/USER_CREDENTIALS.sql" },
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
class AuthControllerTest extends ControllerTest {

    @Override
    public String urlBase() {
        return "/auth";
    }

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private AuthJwtService authJwtService;

    @Test
    void Should_ReturnAuthoritiesAndToken_When_SuccessfulLogin() {
        // GIVEN
        final var username = "mrmasterkey";
        final var password = "password";
        final var dto = new CredentialsDto(username, password);
        final var expectedUserId = 1;
        final var expectedFullName = "Keith Master";
        final var expectedAuthorities = UserRole.ADMINISTRATOR_ROLE_GROUP.getAuthorityNames();

        // WHEN
        final var response = webClient
            .post()
            .uri(builder -> url(builder, "/log-in").build())
            .body(Mono.just(dto), CredentialsDto.class)
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(LoginResponseDto.class)
            .value(responseDto -> {
                assertEquals(expectedUserId, responseDto.userId());
                assertEquals(expectedFullName, responseDto.userFullName());
                assertNull(responseDto.userImage());
                assertEqualsIgnoringOrder(expectedAuthorities, responseDto.authorities());
            });

        // AND
        response
            .expectHeader()
            .value(
                "Authorization",
                header -> {
                    assertTrue(header.startsWith(jwtConfig.getAuthorizationHeaderPrefix()));

                    final var token = header.substring(jwtConfig.getAuthorizationHeaderPrefix().length());
                    final var claims = authJwtService.parseClaims(token);

                    assertEquals(username, claims.getSubject());
                    assertEquals(expectedUserId, claims.get(JwtClaimName.USER_ID));

                    final var authorities = (Collection<String>) claims.get(JwtClaimName.AUTHORITIES);
                    assertEqualsIgnoringOrder(expectedAuthorities, authorities);
                }
            );
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnUnauthorized_When_AttemptToLogInWithIncorrectCredentials_Source")
    void Should_ReturnUnauthorized_When_AttemptToLogInWithIncorrectCredentials(String username, String password) {
        // GIVEN
        final var dto = new CredentialsDto(username, password);

        // WHEN
        final var response = webClient
            .post()
            .uri(builder -> url(builder, "/log-in").build())
            .body(Mono.just(dto), CredentialsDto.class)
            .exchange();

        // THEN
        response.expectStatus().isUnauthorized();
    }

    private static Stream<Arguments> Should_ReturnUnauthorized_When_AttemptToLogInWithIncorrectCredentials_Source() {
        return Stream.of(Arguments.of("mrmasterkey", "invalidPassword"), Arguments.of("nonExistentUser", "password"));
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToLogInWithoutBody() {
        // WHEN
        final var response = webClient.post().uri(builder -> url(builder, "/log-in").build()).exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnBadRequest_When_AttemptToLogInWithIncorrectData_Source")
    void Should_ReturnBadRequest_When_AttemptToLogInWithIncorrectData(CredentialsDto dto, Set<String> expectedArguments) {
        // WHEN
        final var response = webClient
            .post()
            .uri(builder -> url(builder, "/log-in").build())
            .body(Mono.just(dto), CredentialsDto.class)
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();

        // AND
        response
            .expectBody(ErrorResponse.class)
            .value(responseDto -> {
                assertEquals(ErrorCode.REQUEST_ARGUMENT_INVALID, responseDto.code());
                assertEqualsIgnoringOrder(expectedArguments, responseDto.arguments());
            });
    }

    private static Stream<Arguments> Should_ReturnBadRequest_When_AttemptToLogInWithIncorrectData_Source() {
        return Stream.of(
            Arguments.of(new CredentialsDto(" ", "password"), Set.of("username")),
            Arguments.of(new CredentialsDto("mrmasterkey", " "), Set.of("password"))
        );
    }

    @Test
    void Should_RegisterUser_When_CorrectRequest() {
        // GIVEN
        final var email = "j.doe@bearinmind.kwezal.com";
        final var password = "password";
        final var firstName = "John";
        final var middleName = "Joe";
        final var lastName = "Doe";
        final var dto = new CreateUserDto(email, password, firstName, lastName, middleName);
        final var expectedUserId = ID_SEQUENCE_START;
        final var expectedFullName = firstName + " " + middleName + " " + lastName;
        final var expectedAuthorities = UserRole.STUDENT.getAuthorityNames();

        // WHEN
        final var response = webClient
            .post()
            .uri(builder -> url(builder, "/sign-up").build())
            .body(Mono.just(dto), CreateUserDto.class)
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(LoginResponseDto.class)
            .value(responseDto -> {
                assertEquals(expectedUserId, responseDto.userId());
                assertEquals(expectedFullName, responseDto.userFullName());
                assertNull(responseDto.userImage());
                assertEquals(expectedAuthorities, responseDto.authorities());
            });

        // AND
        response
            .expectHeader()
            .value(
                "Authorization",
                header -> {
                    assertTrue(header.startsWith(jwtConfig.getAuthorizationHeaderPrefix()));

                    final var token = header.substring(jwtConfig.getAuthorizationHeaderPrefix().length());
                    final var claims = authJwtService.parseClaims(token);

                    assertEquals(email, claims.getSubject());
                    assertEquals(expectedUserId, ((Integer) claims.get(JwtClaimName.USER_ID)).longValue());

                    final var authorities = (Collection<String>) claims.get(JwtClaimName.AUTHORITIES);
                    assertEqualsIgnoringOrder(expectedAuthorities, authorities);
                }
            );
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToRegisterExistingUser() {
        // GIVEN
        final var email = "mrmasterkey@bearinmind.kwezal.com";
        final var password = "password";
        final var firstName = "Keith";
        final var lastName = "Master";
        final var dto = new CreateUserDto(email, password, firstName, lastName, null);

        final var expectedErrorCode = ErrorCode.USER_EXISTS;
        final var expectedArguments = Set.of("email");

        // WHEN
        final var response = webClient
            .post()
            .uri(builder -> url(builder, "/sign-up").build())
            .body(Mono.just(dto), CreateUserDto.class)
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

    @Test
    void Should_ReturnBadRequest_When_AttemptToRegisterUserWithoutBody() {
        // WHEN
        final var response = webClient.post().uri(builder -> url(builder, "/sign-up").build()).exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnBadRequest_When_AttemptToRegisterUserWithIncorrectData_Source")
    void Should_ReturnBadRequest_When_AttemptToRegisterUserWithIncorrectData(CreateUserDto dto, Set<String> expectedArguments) {
        // WHEN
        final var response = webClient
            .post()
            .uri(builder -> url(builder, "/sign-up").build())
            .body(Mono.just(dto), CreateUserDto.class)
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();

        // AND
        response
            .expectBody(ErrorResponse.class)
            .value(responseDto -> {
                assertEquals(ErrorCode.REQUEST_ARGUMENT_INVALID, responseDto.code());
                assertEqualsIgnoringOrder(expectedArguments, responseDto.arguments());
            });
    }

    private static Stream<Arguments> Should_ReturnBadRequest_When_AttemptToRegisterUserWithIncorrectData_Source() {
        return Stream.of(
            Arguments.of(new CreateUserDto("invalidEmail", "password", "John", "Doe", null), Set.of("email")),
            Arguments.of(new CreateUserDto("j.doe@bearinmind.kwezal.com", " ", "John", "Doe", null), Set.of("password")),
            Arguments.of(new CreateUserDto("j.doe@bearinmind.kwezal.com", "password", " ", "Doe", null), Set.of("firstName")),
            Arguments.of(new CreateUserDto("j.doe@bearinmind.kwezal.com", "password", "John", " ", null), Set.of("lastName"))
        );
    }

    @Test
    void Should_RemoveCookie_When_WebClientLogOut() {
        // GIVEN
        final var cookieName = jwtConfig.getCookieName();

        // WHEN
        final var response = webClient
            .post()
            .uri(builder -> url(builder, "/log-out").queryParam("client", AuthClient.WEB.name()).build())
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectCookie()
            .value(
                cookieName,
                cookie -> {
                    assertEquals("", cookie);
                }
            );
        response.expectCookie().httpOnly(cookieName, true);
        response.expectCookie().secure(cookieName, true);
        response.expectCookie().maxAge(cookieName, Duration.ofSeconds(-1));
    }
}
