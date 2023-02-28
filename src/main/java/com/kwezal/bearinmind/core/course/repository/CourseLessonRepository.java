package com.kwezal.bearinmind.core.course.repository;

import com.kwezal.bearinmind.core.course.model.CourseLesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseLessonRepository extends JpaRepository<CourseLesson, Long> {}
