package com.kwezal.bearinmind.core.course.service;

import static com.kwezal.bearinmind.core.utils.CollectionUtils.addIgnoreNull;
import static com.kwezal.bearinmind.core.utils.RepositoryUtils.fetch;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

import com.kwezal.bearinmind.core.auth.service.LoggedInUserService;
import com.kwezal.bearinmind.core.course.dto.CourseLessonViewDto;
import com.kwezal.bearinmind.core.course.dto.CreateCourseLessonDto;
import com.kwezal.bearinmind.core.course.dto.CreateCourseLessonPartDto;
import com.kwezal.bearinmind.core.course.dto.UpdateCourseLessonDto;
import com.kwezal.bearinmind.core.course.mapper.CourseLessonMapper;
import com.kwezal.bearinmind.core.course.mapper.CourseLessonPartMapper;
import com.kwezal.bearinmind.core.course.model.Course;
import com.kwezal.bearinmind.core.course.model.CourseLesson;
import com.kwezal.bearinmind.core.course.repository.CourseLessonPartRepository;
import com.kwezal.bearinmind.core.course.repository.CourseLessonRepository;
import com.kwezal.bearinmind.core.course.repository.CourseRepository;
import com.kwezal.bearinmind.core.course.repository.CourseUserDataRepository;
import com.kwezal.bearinmind.core.exception.ErrorCode;
import com.kwezal.bearinmind.exception.ForbiddenException;
import com.kwezal.bearinmind.translation.service.TranslationService;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CourseLessonService {

    private static final int FIRST_ORDINAL = 1;

    private static final String LESSON_TOPIC_TRANSLATION_KEY = "topic";
    private static final String LESSON_DESCRIPTION_TRANSLATION_KEY = "description";

    private final CourseUserDataRepository courseUserDataRepository;
    private final CourseLessonRepository courseLessonRepository;
    private final CourseLessonPartRepository courseLessonPartRepository;
    private final CourseRepository courseRepository;

    private final CourseLessonMapper courseLessonMapper;
    private final CourseLessonPartMapper courseLessonPartMapper;

    private final LoggedInUserService loggedInUserService;
    private final TranslationService translationService;
    private final CourseLessonValidationService courseLessonValidationService;
    private final CourseValidationService courseValidationService;

    @Transactional(readOnly = false)
    public Long createLesson(final Long courseId, final CreateCourseLessonDto lesson) {
        final var course = fetchCourseBy(courseId);
        courseLessonValidationService.validateCourseLessonDates(course.getStartDateTime(), course.getEndDateTime(), lesson);
        courseLessonValidationService.validateCoursePartDto(lesson);

        final var lastOrdinal = course
            .getLessons()
            .stream()
            .map(CourseLesson::getOrdinal)
            .max(Integer::compare)
            .map(ordinal -> ordinal + FIRST_ORDINAL)
            .orElse(FIRST_ORDINAL);
        return createLesson(course, lesson, lastOrdinal);
    }

    @Transactional(readOnly = false)
    public void createLesson(final Long courseId, final List<CreateCourseLessonDto> lessons) {
        final var course = fetchCourseBy(courseId);
        if (isNotEmpty(lessons)) {
            var courseLessonOrdinal = FIRST_ORDINAL;
            for (CreateCourseLessonDto createCourseLessonDto : lessons) {
                createLesson(course, createCourseLessonDto, courseLessonOrdinal);
                courseLessonOrdinal++;
            }
        }
    }

    private Long createLesson(Course course, CreateCourseLessonDto createCourseLessonDto, int courseLessonOrdinal) {
        final var fieldIdentifiers = translationService.createMultilingualTranslations(
            createCourseLessonDto.translations(),
            Set.of(LESSON_TOPIC_TRANSLATION_KEY),
            Set.of(LESSON_DESCRIPTION_TRANSLATION_KEY)
        );

        final var lesson = courseLessonMapper.map(
            createCourseLessonDto,
            fieldIdentifiers.get(LESSON_TOPIC_TRANSLATION_KEY),
            fieldIdentifiers.get(LESSON_DESCRIPTION_TRANSLATION_KEY),
            courseLessonOrdinal
        );
        lesson.setCourse(course);
        final var savedCourseLesson = courseLessonRepository.save(lesson);

        if (nonNull(createCourseLessonDto.parts())) {
            createLessonParts(createCourseLessonDto.parts(), lesson);
        }

        return savedCourseLesson.getId();
    }

    private void createLessonParts(List<CreateCourseLessonPartDto> parts, CourseLesson lesson) {
        var courseLessonPartOrdinal = FIRST_ORDINAL;
        for (final var createCourseLessonPartDto : parts) {
            final var textIdentifier = translationService.createMultilingualTranslation(createCourseLessonPartDto.text());

            var lessonPart = courseLessonPartMapper.map(
                textIdentifier,
                createCourseLessonPartDto.attachments(),
                courseLessonPartOrdinal
            );
            lessonPart.setLesson(lesson);
            courseLessonPartRepository.save(lessonPart);
            courseLessonPartOrdinal++;
        }
    }

    @Transactional(readOnly = false)
    public void updateLesson(final Long id, final UpdateCourseLessonDto dto) {
        final var userId = loggedInUserService.getLoggedInUserId();
        final var lesson = fetchLessonBy(id);
        final var course = lesson.getCourse();

        courseValidationService.validateIfUserHasOwnerOrTeacherRoleInCourse(course.getId(), userId);
        courseLessonValidationService.validateCourseLessonDates(course.getStartDateTime(), course.getEndDateTime(), dto);

        final var fieldIdentifierMap = new HashMap<String, Integer>();
        fieldIdentifierMap.put(LESSON_TOPIC_TRANSLATION_KEY, lesson.getTopicIdentifier());
        fieldIdentifierMap.put(LESSON_DESCRIPTION_TRANSLATION_KEY, lesson.getDescriptionIdentifier());

        final var updatedFieldIdentifierMap = translationService.updateMultilingualTranslations(
            fieldIdentifierMap,
            dto.translations()
        );

        courseLessonMapper.update(lesson, dto, updatedFieldIdentifierMap.get(LESSON_DESCRIPTION_TRANSLATION_KEY));
        courseLessonRepository.save(lesson);
    }

    public CourseLessonViewDto findCourseLessonViewDtoBy(final Long id) {
        final var authDetails = loggedInUserService.getAuthenticationDetails();
        final var userId = authDetails.userId();
        final var locale = authDetails.locale();

        final var lesson = fetchLessonBy(id);
        final var isInCourse = courseUserDataRepository.existsByCourseIdAndUserId(lesson.getCourse().getId(), userId);

        if (!isInCourse || (nonNull(lesson.getStartDateTime()) && OffsetDateTime.now().isBefore(lesson.getStartDateTime()))) {
            throw new ForbiddenException(CourseLesson.class, Map.of("id", id.toString()), ErrorCode.NO_ACCESS_TO_LESSON);
        }

        final var translations = getTranslations(locale, lesson);
        return courseLessonMapper.mapToCourseLessonViewDto(lesson, translations);
    }

    private CourseLesson fetchLessonBy(final Long id) {
        return fetch(id, courseLessonRepository, CourseLesson.class);
    }

    private Course fetchCourseBy(final Long id) {
        return fetch(id, courseRepository, Course.class);
    }

    private Map<Integer, String> getTranslations(String locale, CourseLesson lesson) {
        final var identifiers = getTranslationIdentifiers(lesson);
        return translationService.findAllIdentifierAndTextByIdentifiersAndLocale(identifiers, locale);
    }

    private Set<Integer> getTranslationIdentifiers(CourseLesson lesson) {
        final var identifiers = new HashSet<Integer>();
        addIgnoreNull(identifiers, lesson.getTopicIdentifier());
        addIgnoreNull(identifiers, lesson.getDescriptionIdentifier());
        for (final var part : lesson.getParts()) {
            addIgnoreNull(identifiers, part.getTextIdentifier());
        }

        return identifiers;
    }
}
