package com.kwezal.bearinmind.core.user.service;

import static com.kwezal.bearinmind.core.utils.RepositoryUtils.fetch;
import static java.util.Objects.isNull;

import com.kwezal.bearinmind.core.auth.service.LoggedInUserService;
import com.kwezal.bearinmind.core.user.dto.CreateOrUpdateUserGroupDto;
import com.kwezal.bearinmind.core.user.dto.UserGroupDto;
import com.kwezal.bearinmind.core.user.dto.UserGroupListItemDto;
import com.kwezal.bearinmind.core.user.mapper.UserGroupMapper;
import com.kwezal.bearinmind.core.user.model.UserGroup;
import com.kwezal.bearinmind.core.user.model.UserGroup_;
import com.kwezal.bearinmind.core.user.repository.UserGroupRepository;
import com.kwezal.bearinmind.core.user.view.UserGroupListItemView;
import com.kwezal.bearinmind.exception.ResourceNotFoundException;
import com.kwezal.bearinmind.translation.service.TranslationService;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserGroupService {

    private final UserGroupRepository userGroupRepository;
    private final UserGroupMapper userGroupMapper;
    private final UserGroupValidationService userGroupValidationService;
    private final UserGroupMemberService userGroupMemberService;

    private final TranslationService translationService;
    private final LoggedInUserService loggedInUserService;

    public long createUserGroup(final CreateOrUpdateUserGroupDto dto) {
        final var nameIdentifier = translationService.createMultilingualTranslation(dto.name(), true);
        var userGroup = userGroupMapper.map(nameIdentifier);
        userGroup = userGroupRepository.save(userGroup);

        userGroupMemberService.addOwnerToGroup(userGroup);

        return userGroup.getId();
    }

    public void updateUserGroup(final Long id, final CreateOrUpdateUserGroupDto dto) {
        final var userId = loggedInUserService.getLoggedInUserId();
        userGroupValidationService.validateIfUserHasWritePermissionToUserGroup(id, userId);

        final var userGroup = fetch(id, userGroupRepository, UserGroup.class);
        final var nameIdentifier = userGroup.getNameIdentifier();

        translationService.updateMultilingualTranslation(nameIdentifier, dto.name());
    }

    public UserGroupDto findUserGroupDtoBy(Long id) {
        final var locale = loggedInUserService.getLoggedInUserLocale();

        final var userGroup = userGroupRepository.findUserGroupWithMembersById(id);
        if (isNull(userGroup)) {
            throw new ResourceNotFoundException(UserGroup.class, Map.of(UserGroup_.ID, id));
        }

        final var translation = translationService.findTextByIdentifierAndLocale(userGroup.getNameIdentifier(), locale);
        return userGroupMapper.mapToUserGroupDto(userGroup, translation);
    }

    public Page<UserGroupListItemDto> findRegisteredUserGroupPage(Integer pageNumber, Integer pageSize) {
        final var authDetails = loggedInUserService.getAuthenticationDetails();
        final var userId = authDetails.userId();
        final var locale = authDetails.locale();

        final var registeredGroups = userGroupRepository.findAllRegisteredUserGroupListItemByUserId(
            userId,
            Pageable.ofSize(pageSize).withPage(pageNumber)
        );
        final var translations = getTranslations(registeredGroups, locale);

        return registeredGroups.map(group ->
            userGroupMapper.mapToUserGroupListItemDto(group, translations.get(group.getNameIdentifier()))
        );
    }

    public Page<UserGroupListItemDto> findAvailableUserGroupPage(Integer pageNumber, Integer pageSize) {
        final var authDetails = loggedInUserService.getAuthenticationDetails();
        final var userId = authDetails.userId();
        final var locale = authDetails.locale();

        final var availableGroups = userGroupRepository.findAllAvailableUserGroupListItemByUserId(
            userId,
            Pageable.ofSize(pageSize).withPage(pageNumber)
        );
        final var translations = getTranslations(availableGroups, locale);

        return availableGroups.map(group ->
            userGroupMapper.mapToUserGroupListItemDto(group, translations.get(group.getNameIdentifier()))
        );
    }

    private Map<Integer, String> getTranslations(Page<UserGroupListItemView> groups, String locale) {
        return translationService.findAllIdentifierAndTextByIdentifiersAndLocale(
            groups.stream().map(UserGroupListItemView::getNameIdentifier).collect(Collectors.toList()),
            locale
        );
    }
}
