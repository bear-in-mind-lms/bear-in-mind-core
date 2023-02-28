package com.kwezal.bearinmind.core.course.mapper;

import com.kwezal.bearinmind.core.course.dto.*;
import com.kwezal.bearinmind.core.course.model.CourseLesson;
import com.kwezal.bearinmind.core.course.model.CourseLessonPart;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface CourseLessonMapper {
    CourseLesson map(CreateCourseLessonDto dto);

    CourseLessonCardDto mapToCourseLessonCardDto(CourseLesson lesson, String topic, String description);

    @Mapping(target = "image", ignore = true)
    @Mapping(target = "parts", ignore = true)
    CourseLesson map(CreateCourseLessonDto dto, Integer topicIdentifier, Integer descriptionIdentifier, Integer ordinal);

    CourseLessonCardDto map(CourseLesson lesson, String topic, String description);

    void update(@MappingTarget CourseLesson entity, UpdateCourseLessonDto dto, Integer descriptionIdentifier);

    @Mapping(target = "parts", source = "parts")
    CourseLessonViewDto mapToCourseLessonViewDto(
        CourseLesson lesson,
        String topic,
        String description,
        List<CourseLessonPartDto> parts
    );

    default CourseLessonViewDto mapToCourseLessonViewDto(CourseLesson lesson, Map<Integer, String> translations) {
        return mapToCourseLessonViewDto(
            lesson,
            translations.get(lesson.getTopicIdentifier()),
            translations.get(lesson.getDescriptionIdentifier()),
            lesson
                .getParts()
                .stream()
                .map(part -> mapToCourseLessonPartDto(part, translations.get(part.getTextIdentifier())))
                .toList()
        );
    }

    CourseLessonPartDto mapToCourseLessonPartDto(CourseLessonPart part, String text);
}
