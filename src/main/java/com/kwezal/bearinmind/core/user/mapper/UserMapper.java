package com.kwezal.bearinmind.core.user.mapper;

import static org.apache.logging.log4j.util.Strings.isBlank;

import com.kwezal.bearinmind.core.course.mapper.CourseMapper;
import com.kwezal.bearinmind.core.course.view.UserCourseView;
import com.kwezal.bearinmind.core.user.dto.*;
import com.kwezal.bearinmind.core.user.model.User;
import com.kwezal.bearinmind.core.user.view.UserGroupListItemView;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

// FIXME Explicitly defining the OffsetDateTime import may be redundant in the future, but due to mapstruct bug it is necessary now
@Mapper(imports = OffsetDateTime.class)
public interface UserMapper {
    CourseMapper courseMapper = Mappers.getMapper(CourseMapper.class);
    UserGroupMapper userGroupMapper = Mappers.getMapper(UserGroupMapper.class);

    @Mapping(target = "userCredentials.username", source = "createUserDto.email")
    @Mapping(target = "userCredentials.active", constant = "true")
    @Mapping(target = "registrationDateTime", expression = "java( OffsetDateTime.now() )")
    User map(CreateUserDto createUserDto, String locale);

    @Mapping(target = "username", source = "userCredentials.username")
    @Mapping(target = "password", source = "userCredentials.password")
    @Mapping(target = "role", source = "userCredentials.role")
    @Mapping(target = "active", source = "userCredentials.active")
    UserDto mapToUserDto(User user);

    User update(@MappingTarget User user, UpdateUserDto updateUserDto);

    @Mapping(target = "name", source = "user", qualifiedByName = "joinFirstAndMiddleAndLastName")
    @Mapping(target = "courses", expression = "java( courseMapper.mapToUserCourseDtos(courses, translations) )")
    @Mapping(target = "groups", expression = "java( userGroupMapper.mapToUserGroupListItemDtos(groups, translations) )")
    UserViewDto mapToUserViewDto(
        User user,
        List<UserCourseView> courses,
        List<UserGroupListItemView> groups,
        Map<Integer, String> translations
    );

    @Mapping(
        target = "registeredGroups",
        expression = "java( userGroupMapper.mapToUserGroupListItemDtos(registeredGroups, translations) )"
    )
    @Mapping(
        target = "availableGroups",
        expression = "java( userGroupMapper.mapToUserGroupListItemDtos(availableGroups, translations) )"
    )
    UserMainViewDto mapToUserMainViewDto(
        Map<Integer, String> translations,
        List<UserGroupListItemView> registeredGroups,
        List<UserGroupListItemView> availableGroups,
        boolean hasTeachers,
        boolean hasStudents
    );

    @Named("joinFirstAndMiddleAndLastName")
    default String joinFirstAndMiddleAndLastName(User user) {
        return String.join(
            " ",
            isBlank(user.getMiddleName())
                ? List.of(user.getFirstName(), user.getLastName())
                : List.of(user.getFirstName(), user.getMiddleName(), user.getLastName())
        );
    }
}
