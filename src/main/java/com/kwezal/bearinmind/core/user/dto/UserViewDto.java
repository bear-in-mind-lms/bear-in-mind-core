package com.kwezal.bearinmind.core.user.dto;

import com.kwezal.bearinmind.core.course.dto.UserCourseDto;
import java.time.OffsetDateTime;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record UserViewDto(
    @NotBlank String name,
    String title,
    String image,
    @NotNull OffsetDateTime registrationDateTime,
    @NotNull List<UserCourseDto> courses,
    @NotNull List<UserGroupListItemDto> groups
) {}
