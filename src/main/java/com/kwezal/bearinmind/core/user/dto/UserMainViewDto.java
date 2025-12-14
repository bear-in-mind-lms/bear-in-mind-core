package com.kwezal.bearinmind.core.user.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record UserMainViewDto(
    @NotNull List<UserGroupListItemDto> registeredGroups,
    @NotNull List<UserGroupListItemDto> availableGroups,
    @NotNull Boolean hasTeachers,
    @NotNull Boolean hasStudents
) {}
