package com.kwezal.bearinmind.core.user.service;

import com.kwezal.bearinmind.core.exceptions.InvalidRequestDataException;
import com.kwezal.bearinmind.core.user.model.User;
import com.kwezal.bearinmind.core.user.model.UserGroupMember;
import com.kwezal.bearinmind.core.user.repository.UserGroupMemberRepository;
import com.kwezal.bearinmind.core.user.repository.UserRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
class UserValidationService {

    private final UserRepository userRepository;
    private final UserGroupMemberRepository userGroupMemberRepository;

    /**
     * Throws exception if a given user belongs to a given user group.
     *
     * @param groupId   user group ID
     * @param userId    user ID
     * @param errorCode error code when an exception is thrown
     * @throws InvalidRequestDataException if validation fails
     */
    void validateIfUserDoesNotBelongToUserGroup(final Long groupId, final Long userId, final String errorCode) {
        if (userGroupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new InvalidRequestDataException(
                UserGroupMember.class,
                Map.of("groupId", groupId, "userId", userId),
                errorCode,
                List.of("groupId", "userId")
            );
        }
    }

    /**
     * Throws exception if a given user exists.
     *
     * @param email     user email
     * @param errorCode error code when an exception is thrown
     * @throws InvalidRequestDataException if validation fails
     */
    void validateIfUserDoesNotExist(final String email, final String errorCode) {
        if (userRepository.existsByUserCredentialsUsernameOrEmailAndUserCredentialsActiveTrue(email, email)) {
            throw new InvalidRequestDataException(User.class, "email", email, errorCode, List.of("email"));
        }
    }
}
