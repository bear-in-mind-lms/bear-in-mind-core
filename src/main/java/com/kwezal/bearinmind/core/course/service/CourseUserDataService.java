package com.kwezal.bearinmind.core.course.service;

import com.kwezal.bearinmind.core.auth.service.LoggedInUserService;
import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import com.kwezal.bearinmind.core.course.mapper.CourseUserDataMapper;
import com.kwezal.bearinmind.core.course.model.Course;
import com.kwezal.bearinmind.core.course.repository.CourseRepository;
import com.kwezal.bearinmind.core.course.repository.CourseUserDataRepository;
import com.kwezal.bearinmind.core.user.model.User;
import com.kwezal.bearinmind.core.user.repository.UserRepository;
import com.kwezal.bearinmind.core.utils.RepositoryUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class CourseUserDataService {

    private final CourseUserDataRepository courseUserDataRepository;
    private final CourseUserDataMapper courseUserDataMapper;

    private final CourseValidationService courseValidationService;
    private final LoggedInUserService loggedInUserService;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = false)
    public void enrollUserInCourse(final Long courseId) {
        final var userId = loggedInUserService.getLoggedInUserId();

        courseValidationService.validateIfUserBelongsToCourseGroup(courseId, userId);
        courseValidationService.validateIfUserIsNotEnrolledInCourse(courseId, userId);

        final var user = RepositoryUtils.fetch(userId, userRepository, User.class);
        final var course = RepositoryUtils.fetch(courseId, courseRepository, Course.class);

        final var userData = courseUserDataMapper.map(course, user, CourseRole.STUDENT);
        courseUserDataRepository.save(userData);
    }
}
