package com.kwezal.bearinmind.core.course.service;

import static java.util.Objects.nonNull;

import com.kwezal.bearinmind.core.course.dto.CreateCourseDto;
import com.kwezal.bearinmind.core.course.dto.UpdateCourseDto;
import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import com.kwezal.bearinmind.core.course.model.Course;
import com.kwezal.bearinmind.core.course.model.CourseUserData;
import com.kwezal.bearinmind.core.course.model.CourseUserGroup;
import com.kwezal.bearinmind.core.course.repository.CourseUserDataRepository;
import com.kwezal.bearinmind.core.course.repository.CourseUserGroupRepository;
import com.kwezal.bearinmind.core.exception.ErrorCode;
import com.kwezal.bearinmind.exception.ForbiddenException;
import com.kwezal.bearinmind.exception.InvalidRequestDataException;
import java.time.OffsetDateTime;
import java.util.EnumSet;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
class CourseValidationService {

    private final CourseLessonValidationService courseLessonValidationService;

    private final CourseUserDataRepository courseUserDataRepository;
    private final CourseUserGroupRepository courseUserGroupRepository;

    /**
     * Throws exception if a given user does not belong to any user group associated with a given course.
     *
     * @param courseId course ID
     * @param userId   user ID
     * @throws ForbiddenException if validation fails
     */
    void validateIfUserBelongsToCourseGroup(final Long courseId, final Long userId) {
        if (!courseUserGroupRepository.existsByCourseIdAndUserId(courseId, userId)) {
            throw new ForbiddenException(CourseUserGroup.class, Map.of("courseId", courseId, "userId", userId));
        }
    }

    /**
     * Throws exception if a given user does not have the owner or teacher role in a given course.
     *
     * @param courseId course ID
     * @param userId   user ID
     * @throws InvalidRequestDataException if validation fails
     */
    void validateIfUserHasOwnerOrTeacherRoleInCourse(final Long courseId, final Long userId) {
        final var isInCourse = courseUserDataRepository.existsByCourseIdAndUserIdAndRoleIn(
            courseId,
            userId,
            EnumSet.of(CourseRole.OWNER, CourseRole.TEACHER)
        );

        if (!isInCourse) {
            throw new ForbiddenException(Course.class, Map.of("id", userId.toString()), ErrorCode.NO_ACCESS_TO_LESSON);
        }
    }

    /**
     * Throws exception if a given user is enrolled in a given course.
     *
     * @param courseId course ID
     * @param userId   user ID
     * @throws InvalidRequestDataException if validation fails
     */
    void validateIfUserIsNotEnrolledInCourse(final Long courseId, final Long userId) {
        if (courseUserDataRepository.existsByCourseIdAndUserId(courseId, userId)) {
            throw new InvalidRequestDataException(
                CourseUserData.class,
                Map.of("courseId", courseId, "userId", userId),
                ErrorCode.CANNOT_ENROLL
            );
        }
    }

    /**
     * Throws exception if a given data transfer object is invalid.
     * All course and lesson date fields should be correctly related to each other.
     * All lesson parts should have a text, an attachment, or both.
     *
     * @param dto data for course creation
     * @throws InvalidRequestDataException if validation fails
     */
    void validateCreateCourseDto(final CreateCourseDto dto) {
        validateIfDatesAreCorrectlyRelatedToEachOther(
            dto.startDateTime(),
            dto.endDateTime(),
            dto.registrationClosingDateTime()
        );
        courseLessonValidationService.validateCourseLessonDates(dto.startDateTime(), dto.endDateTime(), dto.lessons());
        courseLessonValidationService.validateCoursePartDto(dto.lessons());
    }

    /**
     * Throws exception if a given data transfer object is invalid.
     * Course date fields should be correctly related to each other.
     *
     * @param dto data for course update
     * @throws InvalidRequestDataException if validation fails
     */
    void validateUpdateCourseDto(final UpdateCourseDto dto) {
        validateIfDatesAreCorrectlyRelatedToEachOther(
            dto.startDateTime(),
            dto.endDateTime(),
            dto.registrationClosingDateTime()
        );
    }

    private void validateIfDatesAreCorrectlyRelatedToEachOther(
        final OffsetDateTime startDateTime,
        final OffsetDateTime endDateTime,
        final OffsetDateTime registrationClosingDateTime
    ) {
        validateIfStartDateIsBeforeEndDate(startDateTime, endDateTime);
        validateIfRegistrationClosingDateIsBeforeEndDate(registrationClosingDateTime, endDateTime);
        validateIfStartDateIsBeforeRegistrationClosingDate(startDateTime, registrationClosingDateTime);

        final var now = OffsetDateTime.now();
        validateIfNowIsBeforeEndDate(now, endDateTime);
        validateIfNowIsBeforeRegistrationClosingDate(now, registrationClosingDateTime);
    }

    private void validateIfStartDateIsBeforeEndDate(final OffsetDateTime startDateTime, final OffsetDateTime endDateTime) {
        if (nonNull(startDateTime) && nonNull(endDateTime) && startDateTime.isAfter(endDateTime)) {
            throw new InvalidRequestDataException(
                CreateCourseDto.class,
                Map.of("startDateTime", startDateTime, "endDateTime", endDateTime),
                ErrorCode.INVALID_COURSE_START_DATE_TIME_IS_AFTER_END_DATE_TIME
            );
        }
    }

    private void validateIfRegistrationClosingDateIsBeforeEndDate(
        final OffsetDateTime registrationClosingDateTime,
        final OffsetDateTime endDateTime
    ) {
        if (nonNull(registrationClosingDateTime) && nonNull(endDateTime)) {
            if (registrationClosingDateTime.isAfter(endDateTime)) {
                throw new InvalidRequestDataException(
                    CreateCourseDto.class,
                    Map.of("registrationClosingDateTime", registrationClosingDateTime, "endDateTime", endDateTime),
                    ErrorCode.INVALID_COURSE_REGISTRATION_CLOSING_DATE_TIME_IS_AFTER_END_DATE_TIME
                );
            }
        }
    }

    private void validateIfNowIsBeforeEndDate(final OffsetDateTime now, final OffsetDateTime endDateTime) {
        if (nonNull(endDateTime)) {
            if (now.isAfter(endDateTime)) {
                throw new InvalidRequestDataException(
                    CreateCourseDto.class,
                    Map.of("endDateTime", endDateTime),
                    ErrorCode.INVALID_COURSE_END_DATE_TIME_IS_BEFORE_NOW_TIME
                );
            }
        }
    }

    private void validateIfStartDateIsBeforeRegistrationClosingDate(
        final OffsetDateTime startDateTime,
        final OffsetDateTime registrationClosingDateTime
    ) {
        if (nonNull(startDateTime) && nonNull(registrationClosingDateTime)) {
            if (startDateTime.isAfter(registrationClosingDateTime)) {
                throw new InvalidRequestDataException(
                    CreateCourseDto.class,
                    Map.of("startDateTime", startDateTime, "registrationClosingDateTime", registrationClosingDateTime),
                    ErrorCode.INVALID_COURSE_START_DATE_TIME_IS_AFTER_REGISTRATION_CLOSING_DATE_TIME
                );
            }
        }
    }

    private void validateIfNowIsBeforeRegistrationClosingDate(
        final OffsetDateTime now,
        final OffsetDateTime registrationClosingDateTime
    ) {
        if (nonNull(registrationClosingDateTime)) {
            if (now.isAfter(registrationClosingDateTime)) {
                throw new InvalidRequestDataException(
                    CreateCourseDto.class,
                    Map.of("registrationClosingDateTime", registrationClosingDateTime),
                    ErrorCode.INVALID_COURSE_REGISTRATION_CLOSING_DATE_TIME_IS_BEFORE_NOW_TIME
                );
            }
        }
    }
}
