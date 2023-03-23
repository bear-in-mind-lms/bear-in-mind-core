package com.kwezal.bearinmind.core.user.service;

import com.kwezal.bearinmind.core.user.dto.CreateOrUpdateUserGroupDto;
import com.kwezal.bearinmind.core.user.enumeration.UserGroupRole;
import com.kwezal.bearinmind.core.user.repository.UserGroupMemberRepository;
import com.kwezal.bearinmind.exception.ForbiddenException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
class UserGroupValidationService {

    private final UserGroupMemberRepository userGroupMemberRepository;

    /**
     * Throws exception if a given user does not have write permission to a given user group.
     *
     * @param groupId group ID
     * @param userId  user ID
     * @throws ForbiddenException if validation fails
     */
    void validateIfUserHasWritePermissionToUserGroup(final Long groupId, final long userId) {
        if (!userGroupMemberRepository.existsByGroupIdAndUserIdAndRole(groupId, userId, UserGroupRole.OWNER)) {
            throw new ForbiddenException(CreateOrUpdateUserGroupDto.class, Map.of("groupId", groupId, "userId", userId));
        }
    }
}
