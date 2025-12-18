package com.kwezal.bearinmind.core.course.dto;

import com.kwezal.bearinmind.translation.validation.annotation.Locale;
import java.time.OffsetDateTime;
import java.util.Map;

/**
 * @param translations  mapping of locale to field texts; expected field keys are "topic" and "description"
 * @param startDateTime lesson start date-time
 */
public record UpdateCourseLessonDto(
    Map<@Locale String, Map<String, String>> translations,

    OffsetDateTime startDateTime
) {}
