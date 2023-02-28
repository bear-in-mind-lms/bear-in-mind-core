package com.kwezal.bearinmind.core.course.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record CourseLessonViewDto(
    @NotBlank String topic,

    String description,

    String image,

    @NotNull List<CourseLessonPartDto> parts
) {}
