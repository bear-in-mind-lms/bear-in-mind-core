package com.kwezal.bearinmind.core.course.repository;

import com.kwezal.bearinmind.core.course.model.CourseLessonPart;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseLessonPartRepository extends JpaRepository<@NonNull CourseLessonPart, @NonNull Long> {}
