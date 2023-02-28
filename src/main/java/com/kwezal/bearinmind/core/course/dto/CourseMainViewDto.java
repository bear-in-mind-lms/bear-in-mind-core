package com.kwezal.bearinmind.core.course.dto;

import java.util.List;
import javax.validation.constraints.NotNull;

public record CourseMainViewDto(
    @NotNull List<CourseListItemDto> activeCourses,

    @NotNull List<CourseListItemDto> availableCourses,

    @NotNull List<CourseListItemDto> completedCourses
) {}
