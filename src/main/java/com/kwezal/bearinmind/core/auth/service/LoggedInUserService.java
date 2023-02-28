package com.kwezal.bearinmind.core.auth.service;

import com.kwezal.bearinmind.core.config.security.JwtAuthenticationDetails;
import com.kwezal.bearinmind.core.user.dto.UserRole;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class LoggedInUserService {

    public String getLoggedInUserLocale() {
        return getAuthenticationDetails().locale();
    }

    public long getLoggedInUserId() {
        return getAuthenticationDetails().userId();
    }

    public Set<UserRole> getLoggedInUserRoles() {
        return getAuthentication()
            .getAuthorities()
            .stream()
            .filter(authority -> authority.getAuthority().startsWith("ROLE_"))
            .map(role -> UserRole.valueOfAuthority(role.getAuthority()))
            .collect(Collectors.toSet());
    }

    public JwtAuthenticationDetails getAuthenticationDetails() {
        return (JwtAuthenticationDetails) getAuthentication().getDetails();
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
