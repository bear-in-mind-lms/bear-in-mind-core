package com.kwezal.bearinmind.core.course.dto;

import com.kwezal.bearinmind.core.validation.annotation.Locale;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotEmpty;

/**
 * @param translations  mapping of locale to field texts; expected field keys are "topic" and "description"
 * @param startDateTime lesson start date-time
 * @param parts         lesson parts data
 */
public record CreateCourseLessonDto(
    @NotEmpty Map<@Locale String, Map<String, String>> translations,

    OffsetDateTime startDateTime,

    List<CreateCourseLessonPartDto> parts
) {}
