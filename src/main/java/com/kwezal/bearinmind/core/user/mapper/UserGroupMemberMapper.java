package com.kwezal.bearinmind.core.user.mapper;

import com.kwezal.bearinmind.core.user.dto.UserListItemDto;
import com.kwezal.bearinmind.core.user.enumeration.UserGroupRole;
import com.kwezal.bearinmind.core.user.model.User;
import com.kwezal.bearinmind.core.user.model.UserGroup;
import com.kwezal.bearinmind.core.user.model.UserGroupMember;
import java.time.OffsetDateTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

// FIXME Explicitly defining the OffsetDateTime import may be redundant in the future, but due to mapstruct bug it is necessary now
@Mapper(imports = OffsetDateTime.class)
public interface UserGroupMemberMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "registrationDateTime", expression = "java( OffsetDateTime.now() )")
    UserGroupMember map(UserGroup group, User user, UserGroupRole role);

    @Mapping(target = "id", source = "user.id")
    @Mapping(target = "name", source = "user", qualifiedByName = "joinFirstAndLastName")
    @Mapping(target = "image", source = "user.image")
    UserListItemDto mapToUserListItemDto(UserGroupMember userGroupMember);

    @Named("joinFirstAndLastName")
    default String joinFirstAndLastName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
