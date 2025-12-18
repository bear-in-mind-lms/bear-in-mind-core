package com.kwezal.bearinmind.core.auth.service;

import com.kwezal.bearinmind.core.auth.JwtConfig;
import com.kwezal.bearinmind.core.auth.dto.CredentialsDto;
import com.kwezal.bearinmind.core.auth.dto.LoginResponseDto;
import com.kwezal.bearinmind.core.auth.enumeration.AuthClient;
import com.kwezal.bearinmind.core.exception.ErrorCode;
import com.kwezal.bearinmind.core.user.dto.CreateUserDto;
import com.kwezal.bearinmind.core.user.mapper.UserMapper;
import com.kwezal.bearinmind.core.user.model.User;
import com.kwezal.bearinmind.core.user.repository.UserRepository;
import com.kwezal.bearinmind.core.user.service.UserService;
import com.kwezal.bearinmind.exception.AuthorizationException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final JwtConfig jwtConfig;
    private final AuthJwtService authJwtService;
    private final PasswordEncoder passwordEncoder;

    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public LoginResponseDto logIn(
        final HttpServletResponse response,
        final CredentialsDto credentials,
        final AuthClient authClient
    ) {
        final var username = credentials.username();
        final var password = credentials.password();

        final var optionalUser = userRepository.findByUserCredentialsUsernameAndUserCredentialsActiveTrue(username);
        if (optionalUser.isEmpty()) {
            throw incorrectCredentialsException(username, password);
        }

        final var user = optionalUser.get();
        final var userCredentials = user.getUserCredentials();
        if (!passwordEncoder.matches(password, userCredentials.getPassword())) {
            throw incorrectCredentialsException(username, password);
        }

        final var fullName = userMapper.mapUserToFullName(user);

        return logIn(
            response,
            authClient,
            user.getId(),
            userCredentials.getUsername(),
            fullName,
            userCredentials.getRole().getAuthorityNames(),
            user.getLocale(),
            user.getImage()
        );
    }

    @Transactional(readOnly = false)
    public LoginResponseDto signUp(final HttpServletResponse response, final CreateUserDto dto, final AuthClient authClient) {
        final var user = userService.createUser(dto);
        final var fullName = userMapper.mapToFullName(dto.firstName(), dto.middleName(), dto.lastName());

        return logIn(
            response,
            authClient,
            user.getId(),
            user.getUsername(),
            fullName,
            user.getRole().getAuthorityNames(),
            user.getLocale(),
            user.getImage()
        );
    }

    private LoginResponseDto logIn(
        final HttpServletResponse response,
        final AuthClient authClient,
        final Long id,
        final String username,
        final String fullName,
        final Set<String> authorities,
        final String locale,
        final String image
    ) {
        appendTokenToResponse(response, authClient, username, id, locale, authorities);

        return new LoginResponseDto(id, fullName, image, authorities);
    }

    public void logOut(final HttpServletResponse response, final AuthClient client) {
        if (client == AuthClient.WEB) {
            final var emptyTokenCookie = buildJwtCookie(null, 0);
            response.addCookie(emptyTokenCookie);
        }
    }

    private void appendTokenToResponse(
        final HttpServletResponse response,
        final AuthClient authClient,
        final String username,
        final Long userId,
        final String locale,
        final Set<String> authorities
    ) {
        final var token = authJwtService.buildJwt(username, userId, locale, authorities);

        // Return the Authorization header for the API client and the cookie for the web client
        if (authClient == AuthClient.API) {
            response.addHeader("Authorization", jwtConfig.getAuthorizationHeaderPrefix() + token);
        } else if (authClient == AuthClient.WEB) {
            final var tokenCookie = buildJwtCookie(token);
            response.addCookie(tokenCookie);
        }
    }

    private Cookie buildJwtCookie(final String token) {
        return buildJwtCookie(token, jwtConfig.getLifetimeMinutes() * 60);
    }

    private Cookie buildJwtCookie(final String token, final int maxAge) {
        final var jwtCookie = new Cookie(jwtConfig.getCookieName(), token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setMaxAge(maxAge);
        jwtCookie.setPath("/");
        return jwtCookie;
    }

    private AuthorizationException incorrectCredentialsException(final String username, final String password) {
        return new AuthorizationException(
            User.class,
            Map.of("username", username, "password", password),
            ErrorCode.INCORRECT_CREDENTIALS
        );
    }
}
