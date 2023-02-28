package com.kwezal.bearinmind.core.user.service;

import static com.kwezal.bearinmind.core.utils.RepositoryUtils.fetch;

import com.kwezal.bearinmind.core.auth.service.LoggedInUserService;
import com.kwezal.bearinmind.core.config.ApplicationConfig;
import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import com.kwezal.bearinmind.core.course.repository.CourseRepository;
import com.kwezal.bearinmind.core.course.repository.CourseUserDataRepository;
import com.kwezal.bearinmind.core.course.view.UserCourseView;
import com.kwezal.bearinmind.core.exceptions.ErrorCode;
import com.kwezal.bearinmind.core.translation.service.TranslationService;
import com.kwezal.bearinmind.core.user.dto.*;
import com.kwezal.bearinmind.core.user.mapper.UserMapper;
import com.kwezal.bearinmind.core.user.model.User;
import com.kwezal.bearinmind.core.user.repository.UserCredentialsRepository;
import com.kwezal.bearinmind.core.user.repository.UserGroupRepository;
import com.kwezal.bearinmind.core.user.repository.UserRepository;
import com.kwezal.bearinmind.core.user.view.UserGroupListItemView;
import java.util.EnumSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class UserService {

    private final ApplicationConfig applicationConfig;

    private final UserRepository userRepository;
    private final UserGroupRepository userGroupRepository;
    private final UserCredentialsRepository userCredentialsRepository;
    private final CourseRepository courseRepository;
    private final CourseUserDataRepository courseUserDataRepository;

    private final UserMapper userMapper;

    private final PasswordEncoder passwordEncoder;

    private final TranslationService translationService;
    private final LoggedInUserService loggedInUserService;
    private final UserValidationService userValidationService;

    @Transactional(readOnly = false)
    public UserDto createUser(CreateUserDto createUserDto) {
        userValidationService.validateIfUserDoesNotExist(createUserDto.email(), ErrorCode.USER_EXISTS);
        final var applicationLocale = applicationConfig.getApplicationLocale();
        final var user = userMapper.map(createUserDto, applicationLocale);
        final var encodedPassword = passwordEncoder.encode(createUserDto.password());
        final var userCredentials = user.getUserCredentials();
        userCredentials.setPassword(encodedPassword);
        userCredentials.setRole(UserRole.STUDENT);
        userCredentialsRepository.save(userCredentials);
        userRepository.save(user);
        return userMapper.mapToUserDto(user);
    }

    @Transactional(readOnly = false)
    public void updateUser(UpdateUserDto dto) {
        final var userId = loggedInUserService.getLoggedInUserId();
        final var user = fetchUserBy(userId);
        final var updatedUser = userMapper.update(user, dto);
        userRepository.save(updatedUser);
    }

    public UserViewDto findUserViewDtoBy(Long id) {
        final var authDetails = loggedInUserService.getAuthenticationDetails();
        final var loggedInUserId = authDetails.userId();
        final var locale = authDetails.locale();

        final var user = fetchUserBy(id);

        final var courses = courseRepository.findAllCommonCourseAndRole(loggedInUserId, user.getId());
        final var courseNameIdentifiersStream = courses.stream().map(UserCourseView::getNameIdentifier);

        final var groups = userGroupRepository.findAllCommonUserGroup(loggedInUserId, user.getId());
        final var groupNameIdentifiersStream = groups.stream().map(UserGroupListItemView::getNameIdentifier);

        final var translations = translationService.findAllIdentifierAndTextByIdentifiersAndLocale(
            Stream.concat(courseNameIdentifiersStream, groupNameIdentifiersStream).collect(Collectors.toSet()),
            locale
        );

        return userMapper.mapToUserViewDto(user, courses, groups, translations);
    }

    public UserMainViewDto findUserMainViewDto(Integer listLength) {
        final var authDetails = loggedInUserService.getAuthenticationDetails();
        final var userId = authDetails.userId();
        final var locale = authDetails.locale();

        final var registeredGroups = userGroupRepository
            .findAllRegisteredUserGroupListItemByUserId(userId, Pageable.ofSize(listLength))
            .toList();
        final var availableGroups = userGroupRepository
            .findAllAvailableUserGroupListItemByUserId(userId, Pageable.ofSize(listLength))
            .toList();

        final var translations = translationService.findAllIdentifierAndTextByIdentifiersAndLocale(
            Stream.concat(registeredGroups.stream(), availableGroups.stream()),
            UserGroupListItemView::getNameIdentifier,
            locale
        );

        final boolean hasTeachers;
        final boolean hasStudents;
        // If the user is not a member of any group, then they cannot be enrolled in any course
        // and therefore cannot have teachers or students
        if (registeredGroups.isEmpty()) {
            hasTeachers = false;
            hasStudents = false;
        } else {
            hasTeachers = courseUserDataRepository.existsByUserIdAndRole(userId, CourseRole.STUDENT);
            hasStudents =
                courseUserDataRepository.existsByUserIdAndRoleIn(userId, EnumSet.of(CourseRole.OWNER, CourseRole.TEACHER));
        }

        return userMapper.mapToUserMainViewDto(translations, registeredGroups, availableGroups, hasTeachers, hasStudents);
    }

    public Page<UserListItemDto> findGroupMemberPage(final Integer pageNumber, final Integer pageSize) {
        final var userId = loggedInUserService.getLoggedInUserId();

        return userRepository.findAllGroupMemberUserListItemByUserId(userId, Pageable.ofSize(pageSize).withPage(pageNumber));
    }

    public Page<UserListItemDto> findStudentPage(final Integer pageNumber, final Integer pageSize) {
        final var userId = loggedInUserService.getLoggedInUserId();

        return userRepository.findAllUserListItemByUserIdAndCourseRoleIn(
            userId,
            EnumSet.of(CourseRole.OWNER, CourseRole.TEACHER),
            EnumSet.of(CourseRole.STUDENT),
            Pageable.ofSize(pageSize).withPage(pageNumber)
        );
    }

    public Page<UserListItemDto> findTeacherPage(final Integer pageNumber, final Integer pageSize) {
        final var userId = loggedInUserService.getLoggedInUserId();

        return userRepository.findAllUserListItemByUserIdAndCourseRoleIn(
            userId,
            EnumSet.of(CourseRole.STUDENT),
            EnumSet.of(CourseRole.OWNER, CourseRole.TEACHER),
            Pageable.ofSize(pageSize).withPage(pageNumber)
        );
    }

    private User fetchUserBy(Long id) {
        return fetch(id, userRepository, User.class);
    }
}
