package com.kwezal.bearinmind.core.user.mapper;

import com.kwezal.bearinmind.core.user.dto.UserGroupDto;
import com.kwezal.bearinmind.core.user.dto.UserGroupListItemDto;
import com.kwezal.bearinmind.core.user.dto.UserListItemDto;
import com.kwezal.bearinmind.core.user.model.UserGroup;
import com.kwezal.bearinmind.core.user.model.UserGroupMember;
import com.kwezal.bearinmind.core.user.view.UserGroupListItemView;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

// FIXME Explicitly defining the OffsetDateTime import may be redundant in the future, but due to mapstruct bug it is necessary now
@Mapper(imports = OffsetDateTime.class)
public interface UserGroupMapper {
    UserGroupMemberMapper userGroupMemberMapper = Mappers.getMapper(UserGroupMemberMapper.class);

    @Mapping(target = "creationDateTime", expression = "java( OffsetDateTime.now() )")
    UserGroup map(Integer nameIdentifier);

    UserGroupListItemDto mapToUserGroupListItemDto(UserGroupListItemView view, String name);

    default List<UserGroupListItemDto> mapToUserGroupListItemDtos(
        List<UserGroupListItemView> views,
        Map<Integer, String> translations
    ) {
        return views
            .stream()
            .map(projection -> mapToUserGroupListItemDto(projection, translations.get(projection.getNameIdentifier())))
            .toList();
    }

    default UserListItemDto mapToUserListItemDto(UserGroupMember userGroupMember) {
        return userGroupMemberMapper.mapToUserListItemDto(userGroupMember);
    }

    UserGroupDto mapToUserGroupDto(UserGroup group, String name);
}
