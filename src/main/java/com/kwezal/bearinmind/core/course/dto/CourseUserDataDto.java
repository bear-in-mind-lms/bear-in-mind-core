package com.kwezal.bearinmind.core.course.dto;

import java.time.OffsetDateTime;
import javax.validation.constraints.NotNull;

public record CourseUserDataDto(
    @NotNull Long id,

    @NotNull Long courseId,

    @NotNull Long userId,

    String role,

    @NotNull OffsetDateTime lastAccessDateTime
) {}
