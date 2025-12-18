package com.kwezal.bearinmind.core.user.controller;

import com.kwezal.bearinmind.core.logging.ControllerLogging;
import com.kwezal.bearinmind.core.user.dto.CreateOrUpdateUserGroupDto;
import com.kwezal.bearinmind.core.user.dto.UserGroupDto;
import com.kwezal.bearinmind.core.user.dto.UserGroupListItemDto;
import com.kwezal.bearinmind.core.user.service.UserGroupService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/group")
@ControllerLogging("/user/group")
@RequiredArgsConstructor
@Validated
public class UserGroupController {

    private final UserGroupService userGroupService;

    /**
     * Creates a user group.
     *
     * @param dto data for user group creation
     * @return created user group's ID
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public long createUserGroup(@RequestBody @Validated CreateOrUpdateUserGroupDto dto) {
        return userGroupService.createUserGroup(dto);
    }

    /**
     * Updates a user group.
     *
     * @param id  user group ID
     * @param dto data for user group update
     */
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUserGroup(@PathVariable @Min(1) Long id, @RequestBody @Validated CreateOrUpdateUserGroupDto dto) {
        userGroupService.updateUserGroup(id, dto);
    }

    /**
     * Collects data for a single user group page.
     *
     * @param id user group ID
     * @return data for a single user group page
     */
    @GetMapping("/{id}")
    public UserGroupDto findUserGroupDtoBy(@PathVariable @Min(1) Long id) {
        return userGroupService.findUserGroupDtoBy(id);
    }

    /**
     * Finds a page of available user groups for the logged-in user.
     * A user group is available if the user is not registered in it yet.
     *
     * @param pageNumber page to be returned
     * @param pageSize   number of items to be returned
     * @return user group page
     */
    @GetMapping("/list/registered")
    public Page<@NonNull UserGroupListItemDto> findRegisteredUserGroupPage(
        @RequestParam @Min(0) Integer pageNumber,
        @RequestParam @Min(1) @Max(100) Integer pageSize
    ) {
        return userGroupService.findRegisteredUserGroupPage(pageNumber, pageSize);
    }

    /**
     * Finds a page of available user groups for the logged-in user.
     * A user group is available if the user is not registered in it yet.
     *
     * @param pageNumber page to be returned
     * @param pageSize   number of items to be returned
     * @return user group page
     */
    @GetMapping("/list/available")
    public Page<@NonNull UserGroupListItemDto> findAvailableUserGroupPage(
        @RequestParam @Min(0) Integer pageNumber,
        @RequestParam @Min(1) @Max(100) Integer pageSize
    ) {
        return userGroupService.findAvailableUserGroupPage(pageNumber, pageSize);
    }
}
