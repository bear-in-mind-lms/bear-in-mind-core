package com.kwezal.bearinmind.core.course.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CourseLessonCardDto(
    @NotNull Long id,

    @NotNull Integer ordinal,

    @NotNull String topic,

    String description,

    OffsetDateTime startDateTime
) {}
