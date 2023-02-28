package com.kwezal.bearinmind.core.user.controller;

import com.kwezal.bearinmind.core.logging.ControllerLogging;
import com.kwezal.bearinmind.core.user.service.UserGroupMemberService;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/group")
@ControllerLogging("/user/group")
@RequiredArgsConstructor
@Validated
public class UserGroupMemberController {

    private final UserGroupMemberService userGroupMemberService;

    /**
     * Adds the logged-in user to a given user group.
     *
     * @param groupId group ID
     */
    @PostMapping("/join/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addUserToGroup(@PathVariable @Min(1) Long groupId) {
        userGroupMemberService.addUserToGroup(groupId);
    }
}
