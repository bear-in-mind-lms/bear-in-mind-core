package com.kwezal.bearinmind.core.auth.controller;

import com.kwezal.bearinmind.core.auth.dto.CredentialsDto;
import com.kwezal.bearinmind.core.auth.dto.LoginResponseDto;
import com.kwezal.bearinmind.core.auth.enumeration.AuthClient;
import com.kwezal.bearinmind.core.auth.service.AuthService;
import com.kwezal.bearinmind.core.user.dto.CreateUserDto;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Authenticates and authorizes the user if login credentials are correct.
     * The access token is passed in the Authorization header or in the cookie depending on the client type.
     *
     * @param response    HTTP servlet response
     * @param credentials username and password
     * @param client      type of client
     * @return list of user authorities
     */
    @PostMapping("/log-in")
    public LoginResponseDto logIn(
        HttpServletResponse response,
        @RequestBody @Validated CredentialsDto credentials,
        @RequestParam(required = false, defaultValue = "API") AuthClient client
    ) {
        return authService.logIn(response, credentials, client);
    }

    /**
     * Registers and logs in the user.
     * The access token is passed in the Authorization header or in the cookie depending on the client type.
     *
     * @param createUserDto user data
     * @return created user data
     */
    @PostMapping("/sign-up")
    public LoginResponseDto signUp(
        HttpServletResponse response,
        @RequestBody @Validated CreateUserDto createUserDto,
        @RequestParam(required = false, defaultValue = "API") AuthClient client
    ) {
        return authService.signUp(response, createUserDto, client);
    }
}
