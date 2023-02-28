package com.kwezal.bearinmind.core.course.dto;

import com.kwezal.bearinmind.core.validation.annotation.Locale;
import java.util.Map;

public record CreateCourseLessonPartDto(
    Map<@Locale String, String> text,

    String attachments
) {}
