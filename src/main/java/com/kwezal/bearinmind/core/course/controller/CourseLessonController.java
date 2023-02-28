package com.kwezal.bearinmind.core.course.controller;

import com.kwezal.bearinmind.core.config.security.RoleRequired;
import com.kwezal.bearinmind.core.course.dto.CourseLessonViewDto;
import com.kwezal.bearinmind.core.course.dto.CreateCourseLessonDto;
import com.kwezal.bearinmind.core.course.dto.UpdateCourseLessonDto;
import com.kwezal.bearinmind.core.course.service.CourseLessonService;
import com.kwezal.bearinmind.core.logging.ControllerLogging;
import com.kwezal.bearinmind.core.user.dto.UserRole;
import javax.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/course")
@ControllerLogging("/course")
@RequiredArgsConstructor
public class CourseLessonController {

    private final CourseLessonService courseLessonService;

    /**
     * Creates a course lesson.
     *
     * @param dto data for course lesson creation
     * @return created course lesson's ID
     */
    @RoleRequired(UserRole.TEACHER)
    @PostMapping("/{courseId}/lesson")
    public Long createLesson(@PathVariable Long courseId, @RequestBody @Validated CreateCourseLessonDto dto) {
        return courseLessonService.createLesson(courseId, dto);
    }

    /**
     * Updates a course lesson.
     *
     * @param id  course lesson ID
     * @param dto data for course lesson update
     */
    @RoleRequired(UserRole.TEACHER)
    @PutMapping("/lesson/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLesson(@PathVariable Long id, @RequestBody @Validated UpdateCourseLessonDto dto) {
        courseLessonService.updateLesson(id, dto);
    }

    /**
     * Collects data for a single course lesson page.
     *
     * @param id course lesson ID
     * @return data for a single course lesson page
     */
    @GetMapping("/lesson/{id}")
    public CourseLessonViewDto findCourseLessonViewDtoBy(@PathVariable @Min(1) Long id) {
        return courseLessonService.findCourseLessonViewDtoBy(id);
    }
}
