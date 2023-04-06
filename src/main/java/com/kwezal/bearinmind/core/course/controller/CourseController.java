package com.kwezal.bearinmind.core.course.controller;

import com.kwezal.bearinmind.core.config.security.RoleRequired;
import com.kwezal.bearinmind.core.course.dto.*;
import com.kwezal.bearinmind.core.course.service.CourseService;
import com.kwezal.bearinmind.core.logging.ControllerLogging;
import com.kwezal.bearinmind.core.user.dto.UserRole;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/course")
@ControllerLogging("/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * Creates a course.
     *
     * @param dto data for course creation
     * @return created course's ID
     */
    @RoleRequired(UserRole.TEACHER)
    @PostMapping
    public Long createCourse(@RequestBody @Validated CreateCourseDto dto) {
        return courseService.createCourse(dto);
    }

    /**
     * Updates a course.
     *
     * @param id  course ID
     * @param dto data for course update
     */
    @RoleRequired(UserRole.TEACHER)
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateCourse(@PathVariable @Min(1) Long id, @RequestBody @Validated UpdateCourseDto dto) {
        courseService.updateCourse(id, dto);
    }

    /**
     * Collects data for courses main view.
     *
     * @param listLength number of items in the lists to be returned
     * @return data for courses main view
     */
    @GetMapping("/main-view")
    public CourseMainViewDto findCourseMainViewDto(@RequestParam @Min(1) @Max(10) Integer listLength) {
        return courseService.findCourseMainViewDto(listLength);
    }

    /**
     * Finds a page of active courses for the logged-in user.
     * An active course is one that has not ended and that user is enrolled in.
     *
     * @param pageNumber page to be returned
     * @param pageSize   number of items to be returned
     * @return user page
     */
    @GetMapping("/list/active")
    public Page<CourseListItemDto> findActiveCoursePage(
        @RequestParam @Min(0) Integer pageNumber,
        @RequestParam @Min(1) @Max(100) Integer pageSize
    ) {
        return courseService.findActiveCoursePage(pageNumber, pageSize);
    }

    /**
     * Finds a page of available courses for the logged-in user.
     * Course availability is defined by belonging to a user group assigned to a given, active course.
     *
     * @param pageNumber page to be returned
     * @param pageSize   number of items to be returned
     * @return user page
     */
    @GetMapping("/list/available")
    public Page<CourseListItemDto> findAvailableCoursePage(
        @RequestParam @Min(0) Integer pageNumber,
        @RequestParam @Min(1) @Max(100) Integer pageSize
    ) {
        return courseService.findAvailableCoursePage(pageNumber, pageSize);
    }

    /**
     * Finds a page of completed courses for the logged-in user.
     * A completed course is one that has ended and that user was enrolled in.
     *
     * @param pageNumber page to be returned
     * @param pageSize   number of items to be returned
     * @return user page
     */
    @GetMapping("/list/completed")
    public Page<CourseListItemDto> findCompletedCoursePage(
        @RequestParam @Min(0) Integer pageNumber,
        @RequestParam @Min(1) @Max(100) Integer pageSize
    ) {
        return courseService.findCompletedCoursePage(pageNumber, pageSize);
    }

    /**
     * Collects data for a single course page.
     *
     * @param id course ID
     * @return data for a single course page
     */
    @GetMapping("/{id}")
    public CourseViewDto findCourseViewDtoBy(@PathVariable @Min(1) Long id) {
        return courseService.findCourseViewDtoBy(id);
    }
}
