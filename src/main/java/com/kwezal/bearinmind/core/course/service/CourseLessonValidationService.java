package com.kwezal.bearinmind.core.course.service;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.kwezal.bearinmind.core.course.dto.CreateCourseLessonDto;
import com.kwezal.bearinmind.core.course.dto.CreateCourseLessonPartDto;
import com.kwezal.bearinmind.core.course.dto.UpdateCourseLessonDto;
import com.kwezal.bearinmind.core.exceptions.ErrorCode;
import com.kwezal.bearinmind.core.exceptions.InvalidRequestDataException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
class CourseLessonValidationService {

    void validateCourseLessonDates(
        final OffsetDateTime courseStartDateTime,
        final OffsetDateTime courseEndDateTime,
        final CreateCourseLessonDto lesson
    ) {
        final var lessonStartDateTime = lesson.startDateTime();
        if (areDatesCorrectlyRelatedToEachOther(courseStartDateTime, courseEndDateTime, lessonStartDateTime)) {
            throw invalidCourseLessonStartDateTimeOrEndDateTimeException(
                courseStartDateTime,
                courseEndDateTime,
                List.of(lesson)
            );
        }
    }

    void validateCourseLessonDates(
        final OffsetDateTime courseStartDateTime,
        final OffsetDateTime courseEndDateTime,
        final List<CreateCourseLessonDto> lessons
    ) {
        if (isNotEmpty(lessons)) {
            final var invalidCourseLessons = lessons
                .stream()
                .filter(courseLessonDto -> {
                    final var lessonStartDateTime = courseLessonDto.startDateTime();
                    return areDatesCorrectlyRelatedToEachOther(courseStartDateTime, courseEndDateTime, lessonStartDateTime);
                })
                .toList();

            if (!invalidCourseLessons.isEmpty()) {
                throw invalidCourseLessonStartDateTimeOrEndDateTimeException(
                    courseStartDateTime,
                    courseEndDateTime,
                    invalidCourseLessons
                );
            }
        }
    }

    private InvalidRequestDataException invalidCourseLessonStartDateTimeOrEndDateTimeException(
        final OffsetDateTime courseStartDateTime,
        final OffsetDateTime courseEndDateTime,
        final List<CreateCourseLessonDto> invalidCourseLessons
    ) {
        return new InvalidRequestDataException(
            CreateCourseLessonDto.class,
            Map.of(
                "courseStartDateTime",
                Objects.toString(courseStartDateTime),
                "courseEndDateTime",
                Objects.toString(courseEndDateTime),
                "courseLesson",
                invalidCourseLessons
            ),
            ErrorCode.INVALID_COURSE_LESSON_START_DATE_TIME_OR_END_DATE_TIME,
            List.of("courseStartDateTime", "courseEndDateTime", "courseLesson")
        );
    }

    void validateCourseLessonDates(
        final OffsetDateTime courseStartDateTime,
        final OffsetDateTime courseEndDateTime,
        final UpdateCourseLessonDto lesson
    ) {
        final OffsetDateTime lessonStartDateTime = lesson.startDateTime();
        if (areDatesCorrectlyRelatedToEachOther(courseStartDateTime, courseEndDateTime, lessonStartDateTime)) {
            throw invalidCourseLessonStartDateTimeOrEndDateTimeException(courseStartDateTime, courseEndDateTime, lesson);
        }
    }

    private InvalidRequestDataException invalidCourseLessonStartDateTimeOrEndDateTimeException(
        final OffsetDateTime courseStartDateTime,
        final OffsetDateTime courseEndDateTime,
        final UpdateCourseLessonDto invalidCourseLesson
    ) {
        return new InvalidRequestDataException(
            CreateCourseLessonDto.class,
            Map.of(
                "courseStartDateTime",
                Objects.toString(courseStartDateTime),
                "courseEndDateTime",
                Objects.toString(courseEndDateTime),
                "courseLesson",
                invalidCourseLesson
            ),
            ErrorCode.INVALID_COURSE_LESSON_START_DATE_TIME_OR_END_DATE_TIME,
            List.of("courseStartDateTime", "courseEndDateTime", "courseLesson")
        );
    }

    private boolean areDatesCorrectlyRelatedToEachOther(
        final OffsetDateTime courseStartDateTime,
        final OffsetDateTime courseEndDateTime,
        final OffsetDateTime lessonStartDateTime
    ) {
        return (
            nonNull(lessonStartDateTime) &&
            (nonNull(courseStartDateTime) && lessonStartDateTime.isBefore(courseStartDateTime)) ||
            (nonNull(courseEndDateTime) && lessonStartDateTime.isAfter(courseEndDateTime))
        );
    }

    void validateCoursePartDto(final List<CreateCourseLessonDto> lessons) {
        if (isNotEmpty(lessons)) {
            for (final var lesson : lessons) {
                validateCoursePartDto(lesson);
            }
        }
    }

    void validateCoursePartDto(final CreateCourseLessonDto lesson) {
        final var lessonParts = lesson.parts();
        if (isNotEmpty(lessonParts)) {
            for (final var part : lessonParts) {
                if (isNull(part.attachments()) && isEmpty(part.text())) {
                    throw new InvalidRequestDataException(
                        CreateCourseLessonPartDto.class,
                        "part",
                        part,
                        ErrorCode.INVALID_COURSE_LESSON_PART_ATTACHMENT_OR_TRANSLATIONS,
                        List.of("part")
                    );
                }
            }
        }
    }
}
