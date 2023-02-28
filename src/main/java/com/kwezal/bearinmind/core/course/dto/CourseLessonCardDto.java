package com.kwezal.bearinmind.core.course.dto;

import java.time.OffsetDateTime;
import javax.validation.constraints.NotNull;

public record CourseLessonCardDto(
    @NotNull Long id,

    @NotNull Integer ordinal,

    @NotNull String topic,

    String description,

    OffsetDateTime startDateTime
) {}
