package com.kwezal.bearinmind.core.course.controller;

import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import com.kwezal.bearinmind.core.course.service.CourseUserDataService;
import com.kwezal.bearinmind.core.logging.ControllerLogging;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/course")
@ControllerLogging("/course")
@RequiredArgsConstructor
public class CourseUserDataController {

    private final CourseUserDataService courseUserDataService;

    /**
     * Enrolls the logged-in user in a given course.
     *
     * @param courseId course ID
     */
    @PostMapping("/enroll/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enrollUserInCourse(@PathVariable @Min(1) Long courseId) {
        courseUserDataService.enrollUserInCourse(courseId);
    }

    /**
     * Finds a role in a given course for the logged-in user.
     *
     * @param id course ID
     * @return course role
     */
    @GetMapping("/{id}/role")
    public CourseRole findCourseRoleByCourseId(@PathVariable @Min(1) Long id) {
        return courseUserDataService.findCourseRoleByCourseId(id);
    }

    /**
     * Finds a role in a course with a given lesson for the logged-in user.
     *
     * @param id course lesson ID
     * @return course role
     */
    @GetMapping("/lesson/{id}/role")
    public CourseRole findCourseRoleByLessonId(@PathVariable @Min(1) Long id) {
        return courseUserDataService.findCourseRoleByLessonId(id);
    }
}
