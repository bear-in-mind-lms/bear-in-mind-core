package com.kwezal.bearinmind.core.course.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CourseUserDataDto(
    @NotNull Long id,

    @NotNull Long courseId,

    @NotNull Long userId,

    String role,

    @NotNull OffsetDateTime lastAccessDateTime
) {}
