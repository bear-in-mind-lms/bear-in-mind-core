package com.kwezal.bearinmind.core.course.repository;

import com.kwezal.bearinmind.core.course.model.CourseLesson;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseLessonRepository extends JpaRepository<@NonNull CourseLesson, @NonNull Long> {}
