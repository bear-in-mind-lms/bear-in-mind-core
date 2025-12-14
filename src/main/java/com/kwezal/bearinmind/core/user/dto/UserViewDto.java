package com.kwezal.bearinmind.core.user.dto;

import com.kwezal.bearinmind.core.course.dto.UserCourseDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

public record UserViewDto(
    @NotBlank String name,
    String title,
    String image,
    @NotNull OffsetDateTime registrationDateTime,
    @NotNull List<UserCourseDto> courses,
    @NotNull List<UserGroupListItemDto> groups
) {}
