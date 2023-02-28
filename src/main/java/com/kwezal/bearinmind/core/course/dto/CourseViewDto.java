package com.kwezal.bearinmind.core.course.dto;

import com.kwezal.bearinmind.core.user.dto.UserListItemDto;
import java.time.OffsetDateTime;
import java.util.List;
import javax.validation.constraints.NotNull;

public record CourseViewDto(
    @NotNull String name,

    String description,

    String image,

    @NotNull List<UserListItemDto> teachers,

    @NotNull List<CourseLessonCardDto> lessons,

    OffsetDateTime endDateTime,

    ActiveCourseDto active,

    AvailableCourseDto available,

    CompletedCourseDto completed
) {}
