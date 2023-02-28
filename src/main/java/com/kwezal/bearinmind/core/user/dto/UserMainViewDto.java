package com.kwezal.bearinmind.core.user.dto;

import java.util.List;
import javax.validation.constraints.NotNull;

public record UserMainViewDto(
    @NotNull List<UserGroupListItemDto> registeredGroups,
    @NotNull List<UserGroupListItemDto> availableGroups,
    @NotNull Boolean hasTeachers,
    @NotNull Boolean hasStudents
) {}
