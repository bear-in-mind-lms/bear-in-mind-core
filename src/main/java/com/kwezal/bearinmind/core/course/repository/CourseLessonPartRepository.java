package com.kwezal.bearinmind.core.course.repository;

import com.kwezal.bearinmind.core.course.model.CourseLessonPart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseLessonPartRepository extends JpaRepository<CourseLessonPart, Long> {}
