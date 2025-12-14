package com.kwezal.bearinmind.core.course.dto;

import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserCourseDto(@NotNull Long id, @NotBlank String name, String image, @NotNull CourseRole role) {}
