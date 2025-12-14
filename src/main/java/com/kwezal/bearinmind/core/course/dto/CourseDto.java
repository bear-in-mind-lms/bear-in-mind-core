package com.kwezal.bearinmind.core.course.dto;

import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public record CourseDto(
    @NotNull Long id,

    @NotNull Integer nameIdentifier,

    Integer descriptionIdentifier,

    String image,

    OffsetDateTime startDateTime,

    OffsetDateTime endDateTime,

    @NotNull OffsetDateTime creationDateTime,

    OffsetDateTime registrationClosingDateTime
) {}
