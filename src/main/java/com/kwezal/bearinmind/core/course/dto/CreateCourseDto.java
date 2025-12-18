package com.kwezal.bearinmind.core.course.dto;

import com.kwezal.bearinmind.translation.validation.annotation.Locale;
import jakarta.validation.constraints.NotEmpty;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * @param translations                mapping of locale to field texts; expected field keys are "name" and "description"
 * @param startDateTime               course start date-time
 * @param endDateTime                 course end date-time
 * @param registrationClosingDateTime course registration closing date-time
 * @param lessons                     course lessons data
 */
public record CreateCourseDto(
    @NotEmpty Map<@Locale String, Map<String, String>> translations,

    OffsetDateTime startDateTime,

    OffsetDateTime endDateTime,

    OffsetDateTime registrationClosingDateTime,

    List<CreateCourseLessonDto> lessons
) {}
