package com.kwezal.bearinmind.core.user.service;

import static com.kwezal.bearinmind.core.utils.RepositoryUtils.fetch;

import com.kwezal.bearinmind.core.auth.service.LoggedInUserService;
import com.kwezal.bearinmind.core.exceptions.ErrorCode;
import com.kwezal.bearinmind.core.user.enumeration.UserGroupRole;
import com.kwezal.bearinmind.core.user.mapper.UserGroupMemberMapper;
import com.kwezal.bearinmind.core.user.model.User;
import com.kwezal.bearinmind.core.user.model.UserGroup;
import com.kwezal.bearinmind.core.user.repository.UserGroupMemberRepository;
import com.kwezal.bearinmind.core.user.repository.UserGroupRepository;
import com.kwezal.bearinmind.core.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserGroupMemberService {

    private final UserGroupMemberRepository userGroupMemberRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserValidationService userValidationService;
    private final UserRepository userRepository;

    private final UserGroupMemberMapper userGroupMemberMapper;

    private final LoggedInUserService loggedInUserService;

    @Transactional(readOnly = false)
    public void addUserToGroup(final Long groupId) {
        final var userId = loggedInUserService.getLoggedInUserId();

        userValidationService.validateIfUserDoesNotBelongToUserGroup(groupId, userId, ErrorCode.CANNOT_JOIN_GROUP);

        final var user = fetchUserBy(userId);
        final var userGroup = fetch(groupId, userGroupRepository, UserGroup.class);

        addUserToGroup(userGroup, user, UserGroupRole.MEMBER);
    }

    @Transactional(readOnly = false)
    public void addOwnerToGroup(final UserGroup userGroup) {
        final var userId = loggedInUserService.getLoggedInUserId();
        final var user = fetchUserBy(userId);

        addUserToGroup(userGroup, user, UserGroupRole.OWNER);
    }

    private void addUserToGroup(final UserGroup userGroup, final User user, final UserGroupRole role) {
        final var userGroupMember = userGroupMemberMapper.map(userGroup, user, role);
        userGroupMemberRepository.save(userGroupMember);
    }

    private User fetchUserBy(final Long id) {
        return fetch(id, userRepository, User.class);
    }
}
