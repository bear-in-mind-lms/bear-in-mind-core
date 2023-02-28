package com.kwezal.bearinmind.core.user.repository;

import com.kwezal.bearinmind.core.user.model.UserCredentials;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserCredentialsRepository
    extends JpaRepository<UserCredentials, Long>, JpaSpecificationExecutor<UserCredentials> {
    Optional<UserCredentials> findByUsernameAndActiveTrue(String username);
}
