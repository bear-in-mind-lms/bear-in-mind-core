package com.kwezal.bearinmind.core.auth;

import static com.kwezal.bearinmind.core.utils.AssertionUtils.assertEqualsIgnoringOrder;
import static org.junit.jupiter.api.Assertions.*;

import com.kwezal.bearinmind.core.ControllerTestInterface;
import com.kwezal.bearinmind.core.auth.dto.CredentialsDto;
import com.kwezal.bearinmind.core.auth.dto.LoginResponseDto;
import com.kwezal.bearinmind.core.auth.service.AuthJwtService;
import com.kwezal.bearinmind.core.exception.ErrorCode;
import com.kwezal.bearinmind.core.user.dto.CreateUserDto;
import com.kwezal.bearinmind.core.user.dto.UserDto;
import com.kwezal.bearinmind.core.user.dto.UserRole;
import com.kwezal.bearinmind.exception.response.ErrorResponse;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
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
class AuthControllerTest implements ControllerTestInterface {

    @Override
    public String urlBase() {
        return "/auth";
    }

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private AuthJwtService authJwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void Should_ReturnAuthoritiesAndToken_When_SuccessfulLogin() {
        // GIVEN
        final var username = "mrmasterkey";
        final var password = "password";
        final var dto = new CredentialsDto(username, password);
        final var expectedAuthorities = UserRole.ADMINISTRATOR_ROLE_GROUP
            .getAuthorityNames()
            .stream()
            .sorted()
            .collect(Collectors.toCollection(LinkedHashSet::new));
        final var expectedUserId = 1;

        // WHEN
        final var response = webClient
            .post()
            .uri(builder -> url(builder, "/login").build())
            .body(Mono.just(dto), CredentialsDto.class)
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(LoginResponseDto.class)
            .value(responseDto -> {
                final var responseAuthorities = responseDto
                    .authorities()
                    .stream()
                    .sorted()
                    .collect(Collectors.toCollection(LinkedHashSet::new));
                assertEquals(expectedAuthorities, responseAuthorities);
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

                    final var authorities =
                        ((ArrayList<String>) claims.get(JwtClaimName.AUTHORITIES)).stream()
                            .sorted()
                            .collect(Collectors.toCollection(LinkedHashSet::new));
                    assertEquals(expectedAuthorities, authorities);
                }
            );
    }

    @Test
    void Should_ReturnUnauthorized_When_AttemptToLoginWithIncorrectCredentials() {
        // GIVEN
        final var username = "mrmasterkeyWrong";
        final var password = "passwordWrong";
        final var dto = new CredentialsDto(username, password);

        // WHEN
        final var response = webClient
            .post()
            .uri(builder -> url(builder, "/login").build())
            .body(Mono.just(dto), CredentialsDto.class)
            .exchange();

        // THEN
        response.expectStatus().isUnauthorized();
    }

    @Test
    void Should_RegisterUser_When_CorrectRequest() {
        // GIVEN
        final var email = "email@domain.com";
        final var password = "password";
        final var dto = new CreateUserDto(email, password);

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
            .expectBody(UserDto.class)
            .value(responseDto -> {
                assertNotNull(responseDto.getId());
                assertEquals(email, responseDto.getEmail());
                assertEquals(email, responseDto.getUsername());
                assertTrue(passwordEncoder.matches(password, responseDto.getPassword()));
            });
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToRegisterExistingUser() {
        // GIVEN
        final var email = "mrmasterkey@bearinmind.kwezal.com";
        final var password = "password";
        final var dto = new CreateUserDto(email, password);

        final var expectedErrorCode = ErrorCode.USER_EXISTS;
        final var expectedArguments = Set.of("email");

        // WHEN
        final var response = webClient
            .post()
            .uri(builder -> url(builder, "/sign-up").build())
            .body(Mono.just(dto), UserDto.class)
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
            .body(Mono.just(dto), UserDto.class)
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
            Arguments.of(new CreateUserDto("username", "password"), Set.of("email")),
            Arguments.of(new CreateUserDto("email@domain.com", ""), Set.of("password"))
        );
    }
}
