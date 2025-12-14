package com.kwezal.bearinmind.core.user.controller;

import com.kwezal.bearinmind.core.logging.ControllerLogging;
import com.kwezal.bearinmind.core.user.dto.UpdateUserDto;
import com.kwezal.bearinmind.core.user.dto.UserListItemDto;
import com.kwezal.bearinmind.core.user.dto.UserMainViewDto;
import com.kwezal.bearinmind.core.user.dto.UserViewDto;
import com.kwezal.bearinmind.core.user.service.UserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@ControllerLogging("/user")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    /**
     * Updates a user.
     *
     * @param dto data for user update
     */
    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(@RequestBody @Validated UpdateUserDto dto) {
        userService.updateUser(dto);
    }

    /**
     * Collects data for a single user page.
     *
     * @param id user ID
     * @return data for a single user page
     */
    @GetMapping("/{id}")
    public UserViewDto findUserViewDtoBy(@PathVariable @Min(1) Long id) {
        return userService.findUserViewDtoBy(id);
    }

    /**
     * Collects data for users main view.
     *
     * @param listLength number of items in the lists to be returned
     * @return data for users main view
     */
    @GetMapping("/main-view")
    public UserMainViewDto findUserMainViewDto(@RequestParam @Min(1) @Max(10) Integer listLength) {
        return userService.findUserMainViewDto(listLength);
    }

    /**
     * Finds a page of users who are members of at least one common group with the logged-in user.
     *
     * @param pageNumber page to be returned
     * @param pageSize   number of items to be returned
     * @return user page
     */
    @GetMapping("/list/group-members")
    public Page<@NonNull UserListItemDto> findGroupMemberPage(
        @RequestParam @Min(0) Integer pageNumber,
        @RequestParam @Min(1) @Max(100) Integer pageSize
    ) {
        return userService.findGroupMemberPage(pageNumber, pageSize);
    }

    /**
     * Finds a page of users who are students of the logged-in user in at least one course.
     *
     * @param pageNumber page to be returned
     * @param pageSize   number of items to be returned
     * @return user page
     */
    @GetMapping("/list/students")
    public Page<@NonNull UserListItemDto> findStudentPage(
        @RequestParam @Min(0) Integer pageNumber,
        @RequestParam @Min(1) @Max(100) Integer pageSize
    ) {
        return userService.findStudentPage(pageNumber, pageSize);
    }

    /**
     * Finds a page of users who are teachers of the logged-in user in at least one course.
     *
     * @param pageNumber page to be returned
     * @param pageSize   number of items to be returned
     * @return user page
     */
    @GetMapping("/list/teachers")
    public Page<@NonNull UserListItemDto> findTeacherPage(
        @RequestParam @Min(0) Integer pageNumber,
        @RequestParam @Min(1) @Max(100) Integer pageSize
    ) {
        return userService.findTeacherPage(pageNumber, pageSize);
    }
}
