package com.kwezal.bearinmind.core.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CourseLessonViewDto(
    @NotBlank String topic,

    String description,

    String image,

    @NotNull List<CourseLessonPartDto> parts
) {}
