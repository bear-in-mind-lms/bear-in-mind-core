package com.kwezal.bearinmind.core.course.dto;

import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record UserCourseDto(@NotNull Long id, @NotBlank String name, String image, @NotNull CourseRole role) {}
