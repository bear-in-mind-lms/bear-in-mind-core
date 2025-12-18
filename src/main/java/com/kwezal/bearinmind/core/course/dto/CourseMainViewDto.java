package com.kwezal.bearinmind.core.course.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CourseMainViewDto(
    @NotNull List<CourseListItemDto> conductedCourses,

    @NotNull List<CourseListItemDto> activeCourses,

    @NotNull List<CourseListItemDto> availableCourses,

    @NotNull List<CourseListItemDto> completedCourses
) {}
