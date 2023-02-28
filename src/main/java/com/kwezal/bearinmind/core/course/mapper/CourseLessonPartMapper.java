package com.kwezal.bearinmind.core.course.mapper;

import com.kwezal.bearinmind.core.course.model.CourseLessonPart;
import org.mapstruct.Mapper;

@Mapper
public interface CourseLessonPartMapper {
    CourseLessonPart map(Integer textIdentifier, String attachments, Integer ordinal);
}
