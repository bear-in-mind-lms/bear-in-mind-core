package com.kwezal.bearinmind.core.course.dto;

import com.kwezal.bearinmind.core.user.dto.UserListItemDto;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.List;

public record CourseViewDto(
    @NotNull String name,

    String description,

    String image,

    @NotNull List<UserListItemDto> teachers,

    @NotNull List<CourseLessonCardDto> lessons,

    OffsetDateTime endDateTime,

    ConductedCourseDto conducted,

    ActiveCourseDto active,

    AvailableCourseDto available,

    CompletedCourseDto completed
) {}
