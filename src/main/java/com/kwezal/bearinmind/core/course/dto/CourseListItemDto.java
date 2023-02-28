package com.kwezal.bearinmind.core.course.dto;

import javax.validation.constraints.NotNull;

public record CourseListItemDto(
    @NotNull Long id,

    @NotNull String name,

    String image
) {}
