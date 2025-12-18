package com.kwezal.bearinmind.core.user.repository;

import com.kwezal.bearinmind.core.user.enumeration.UserGroupRole;
import com.kwezal.bearinmind.core.user.model.UserGroupMember;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserGroupMemberRepository extends JpaRepository<@NonNull UserGroupMember, @NonNull Long> {
    Optional<UserGroupMember> findByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupIdAndUserId(Long groupId, Long userId);

    boolean existsByGroupIdAndUserIdAndRole(Long groupId, Long userId, UserGroupRole role);
}
