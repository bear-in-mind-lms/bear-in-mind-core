package com.kwezal.bearinmind.core.course.dto;

import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import jakarta.validation.constraints.NotNull;

public record CreateOrUpdateCourseUserDataDto(@NotNull CourseRole role) {}
