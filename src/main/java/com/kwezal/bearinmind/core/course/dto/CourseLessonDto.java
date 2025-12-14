package com.kwezal.bearinmind.core.course.dto;

import jakarta.validation.constraints.NotNull;

public record CourseLessonDto(
    @NotNull Long id,

    @NotNull Long courseId,

    @NotNull Integer topicIdentifier,

    Integer descriptionIdentifier,

    String image,

    @NotNull Integer ordinal
) {}
