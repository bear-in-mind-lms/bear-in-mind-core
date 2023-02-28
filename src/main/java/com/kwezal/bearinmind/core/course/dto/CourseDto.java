package com.kwezal.bearinmind.core.course.dto;

import java.time.OffsetDateTime;
import javax.validation.constraints.NotNull;

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
