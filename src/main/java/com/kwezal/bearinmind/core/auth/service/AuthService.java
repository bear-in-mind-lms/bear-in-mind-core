package com.kwezal.bearinmind.core.auth.service;

import com.kwezal.bearinmind.core.auth.JwtConfig;
import com.kwezal.bearinmind.core.auth.dto.CredentialsDto;
import com.kwezal.bearinmind.core.auth.dto.LoginResponseDto;
import com.kwezal.bearinmind.core.auth.enumeration.AuthClient;
import com.kwezal.bearinmind.core.exception.ErrorCode;
import com.kwezal.bearinmind.core.user.model.User;
import com.kwezal.bearinmind.core.user.model.UserCredentials_;
import com.kwezal.bearinmind.core.user.repository.UserRepository;
import com.kwezal.bearinmind.exception.AuthorizationException;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDto logIn(HttpServletResponse response, CredentialsDto credentials, AuthClient authClient) {
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

        final var authorities = userCredentials.getRole().getAuthorityNames();
        appendTokenToResponse(response, authClient, username, user.getId(), user.getLocale(), authorities);

        return new LoginResponseDto(authorities);
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
        final var jwtCookie = new Cookie(jwtConfig.getCookieName(), token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setMaxAge(jwtConfig.getLifetimeMinutes() * 60);
        jwtCookie.setPath("/");
        return jwtCookie;
    }

    private AuthorizationException incorrectCredentialsException(final String username, final String password) {
        return new AuthorizationException(
            User.class,
            Map.of(UserCredentials_.USERNAME, username, UserCredentials_.PASSWORD, password),
            ErrorCode.INCORRECT_CREDENTIALS
        );
    }
}
