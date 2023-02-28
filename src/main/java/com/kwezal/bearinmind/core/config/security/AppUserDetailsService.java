package com.kwezal.bearinmind.core.config.security;

import com.kwezal.bearinmind.core.exceptions.ResourceNotFoundException;
import com.kwezal.bearinmind.core.user.model.UserCredentials;
import com.kwezal.bearinmind.core.user.model.UserCredentials_;
import com.kwezal.bearinmind.core.user.repository.UserCredentialsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService {

    private final UserCredentialsRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repository
            .findByUsernameAndActiveTrue(username)
            .map(user ->
                new org.springframework.security.core.userdetails.User(
                    user.getUsername(),
                    user.getPassword(),
                    user.getRole().getAuthorityNames().stream().map(SimpleGrantedAuthority::new).toList()
                )
            )
            .orElseThrow(() -> new ResourceNotFoundException(UserCredentials.class, UserCredentials_.USERNAME, username));
    }
}
