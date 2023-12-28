package com.kwezal.bearinmind.core.course.service;

import static com.kwezal.bearinmind.core.utils.CollectionUtils.addIgnoreNull;
import static com.kwezal.bearinmind.core.utils.RepositoryUtils.fetch;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import com.kwezal.bearinmind.core.auth.service.LoggedInUserService;
import com.kwezal.bearinmind.core.course.dto.*;
import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import com.kwezal.bearinmind.core.course.mapper.CourseLessonMapper;
import com.kwezal.bearinmind.core.course.mapper.CourseMapper;
import com.kwezal.bearinmind.core.course.mapper.CourseUserDataMapper;
import com.kwezal.bearinmind.core.course.model.Course;
import com.kwezal.bearinmind.core.course.repository.CourseRepository;
import com.kwezal.bearinmind.core.course.repository.CourseUserDataRepository;
import com.kwezal.bearinmind.core.course.view.CourseListItemView;
import com.kwezal.bearinmind.core.user.model.User;
import com.kwezal.bearinmind.core.user.repository.UserRepository;
import com.kwezal.bearinmind.translation.service.TranslationService;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CourseService {

    private static final String COURSE_NAME_TRANSLATION_KEY = "name";
    private static final String COURSE_DESCRIPTION_TRANSLATION_KEY = "description";

    private final CourseRepository courseRepository;
    private final CourseUserDataRepository courseUserDataRepository;
    private final CourseValidationService courseValidationService;

    private final CourseLessonService courseLessonService;

    private final CourseMapper courseMapper;
    private final CourseLessonMapper courseLessonMapper;
    private final CourseUserDataMapper courseUserDataMapper;

    private final TranslationService translationService;

    private final LoggedInUserService loggedInUserService;
    private final UserRepository userRepository;

    @Transactional(readOnly = false)
    public Long createCourse(final CreateCourseDto dto) {
        courseValidationService.validateCreateCourseDto(dto);

        final var fieldIdentifiers = translationService.createMultilingualTranslations(
            dto.translations(),
            Set.of(COURSE_NAME_TRANSLATION_KEY),
            Set.of(COURSE_DESCRIPTION_TRANSLATION_KEY)
        );

        var course = courseMapper.map(dto);
        course.setNameIdentifier(fieldIdentifiers.get(COURSE_NAME_TRANSLATION_KEY));
        course.setDescriptionIdentifier(fieldIdentifiers.get(COURSE_DESCRIPTION_TRANSLATION_KEY));
        course = courseRepository.save(course);
        final var courseId = course.getId();

        final var userId = loggedInUserService.getLoggedInUserId();
        final var user = fetch(userId, userRepository, User.class);
        final var userData = courseUserDataMapper.map(course, user, CourseRole.OWNER);
        courseUserDataRepository.save(userData);
        courseLessonService.createLesson(courseId, dto.lessons());

        return courseId;
    }

    @Transactional(readOnly = false)
    public void updateCourse(final Long id, final UpdateCourseDto dto) {
        final var userId = loggedInUserService.getLoggedInUserId();
        final var course = fetchCourseBy(id);

        courseValidationService.validateIfUserHasOwnerOrTeacherRoleInCourse(course.getId(), userId);
        courseValidationService.validateUpdateCourseDto(dto);

        final var fieldIdentifierMap = new HashMap<String, Integer>();
        fieldIdentifierMap.put(COURSE_NAME_TRANSLATION_KEY, course.getNameIdentifier());
        fieldIdentifierMap.put(COURSE_DESCRIPTION_TRANSLATION_KEY, course.getDescriptionIdentifier());

        final var updatedFieldIdentifierMap = translationService.updateMultilingualTranslations(
            fieldIdentifierMap,
            dto.translations()
        );

        courseMapper.update(course, dto, updatedFieldIdentifierMap.get(COURSE_DESCRIPTION_TRANSLATION_KEY));
        courseRepository.save(course);
    }

    public CourseMainViewDto findCourseMainViewDto(final Integer listLength) {
        final var authDetails = loggedInUserService.getAuthenticationDetails();
        final var userId = authDetails.userId();
        final var locale = authDetails.locale();

        final var conductedCourses = courseRepository
            .findAllConductedCourseListItemByUserId(userId, Pageable.ofSize(listLength))
            .toList();
        final var activeCourses = courseRepository
            .findAllActiveCourseListItemByUserId(userId, Pageable.ofSize(listLength))
            .toList();
        final var availableCourses = courseRepository
            .findAllAvailableCourseListItemByUserId(userId, Pageable.ofSize(listLength))
            .toList();
        final var completedCourses = courseRepository
            .findAllCompletedCourseListItemByUserId(userId, Pageable.ofSize(listLength))
            .toList();

        final var translations = translationService.findAllIdentifierAndTextByIdentifiersAndLocale(
            Stream
                .of(conductedCourses.stream(), activeCourses.stream(), availableCourses.stream(), completedCourses.stream())
                .flatMap(s -> s),
            CourseListItemView::getNameIdentifier,
            locale
        );

        return courseMapper.mapToCourseMainViewDto(
            conductedCourses,
            activeCourses,
            availableCourses,
            completedCourses,
            translations
        );
    }

    public Page<CourseListItemDto> findConductedCoursePage(final Integer pageNumber, final Integer pageSize) {
        return findCoursePage(pageNumber, pageSize, courseRepository::findAllConductedCourseListItemByUserId);
    }

    public Page<CourseListItemDto> findActiveCoursePage(final Integer pageNumber, final Integer pageSize) {
        return findCoursePage(pageNumber, pageSize, courseRepository::findAllActiveCourseListItemByUserId);
    }

    public Page<CourseListItemDto> findAvailableCoursePage(final Integer pageNumber, final Integer pageSize) {
        return findCoursePage(pageNumber, pageSize, courseRepository::findAllAvailableCourseListItemByUserId);
    }

    public Page<CourseListItemDto> findCompletedCoursePage(final Integer pageNumber, final Integer pageSize) {
        return findCoursePage(pageNumber, pageSize, courseRepository::findAllCompletedCourseListItemByUserId);
    }

    private Page<CourseListItemDto> findCoursePage(
        final Integer pageNumber,
        final Integer pageSize,
        final BiFunction<Long, Pageable, Page<CourseListItemView>> findAllCourseListItemByUserId
    ) {
        final var authDetails = loggedInUserService.getAuthenticationDetails();
        final var userId = authDetails.userId();
        final var locale = authDetails.locale();

        final var coursePage = findAllCourseListItemByUserId.apply(userId, Pageable.ofSize(pageSize).withPage(pageNumber));
        final var translations = translationService.findAllIdentifierAndTextByIdentifiersAndLocale(
            coursePage.get(),
            CourseListItemView::getNameIdentifier,
            locale
        );

        return coursePage.map(course ->
            courseMapper.mapToCourseListItemDto(course, translations.get(course.getNameIdentifier()))
        );
    }

    public CourseViewDto findCourseViewDtoBy(final Long id) {
        final var authDetails = loggedInUserService.getAuthenticationDetails();
        final var userId = authDetails.userId();
        final var courseRole = courseUserDataRepository.findCourseRoleByCourseIdAndUserId(id, userId).orElse(null);
        final var isInCourse = nonNull(courseRole);

        if (!isInCourse) {
            courseValidationService.validateIfUserBelongsToCourseGroup(id, userId);
        }

        final var locale = authDetails.locale();

        final var course = fetchCourseBy(id);
        final var image = course.getImage();
        final var teachers = courseUserDataRepository.findAllTeacherByCourseId(id);
        final var translations = getTranslations(locale, course);

        final var name = translations.get(course.getNameIdentifier());
        final var description = translations.get(course.getDescriptionIdentifier());
        final var lessons = course
            .getLessons()
            .stream()
            .map(courseLesson ->
                courseLessonMapper.mapToCourseLessonCardDto(
                    courseLesson,
                    translations.get(courseLesson.getTopicIdentifier()),
                    translations.get(courseLesson.getDescriptionIdentifier())
                )
            )
            .toList();

        ConductedCourseDto conducted = null;
        ActiveCourseDto active = null;
        AvailableCourseDto available = null;
        CompletedCourseDto completed = null;

        final var endDateTime = course.getEndDateTime();
        final var now = OffsetDateTime.now();
        final var isCourseActive = isNull(endDateTime) || now.isBefore(endDateTime);
        if (isInCourse) {
            if (!isCourseActive) {
                completed = new CompletedCourseDto();
            } else if (courseRole.isTeacher()) {
                conducted = new ConductedCourseDto();
            } else {
                active = new ActiveCourseDto();
            }
        } else if (isCourseActive) {
            available = new AvailableCourseDto(course.getRegistrationClosingDateTime());
        }

        return new CourseViewDto(
            name,
            description,
            image,
            teachers,
            lessons,
            endDateTime,
            conducted,
            active,
            available,
            completed
        );
    }

    private Map<Integer, String> getTranslations(String locale, Course course) {
        final var identifiers = getTranslationIdentifiers(course);
        return translationService.findAllIdentifierAndTextByIdentifiersAndLocale(identifiers, locale);
    }

    private List<Integer> getTranslationIdentifiers(Course course) {
        final var identifiers = new ArrayList<Integer>();
        addIgnoreNull(identifiers, course.getNameIdentifier());
        addIgnoreNull(identifiers, course.getDescriptionIdentifier());
        course
            .getLessons()
            .forEach(lesson -> {
                addIgnoreNull(identifiers, lesson.getTopicIdentifier());
                addIgnoreNull(identifiers, lesson.getDescriptionIdentifier());
            });

        return identifiers;
    }

    private Course fetchCourseBy(final Long id) {
        return fetch(id, courseRepository, Course.class);
    }
}
