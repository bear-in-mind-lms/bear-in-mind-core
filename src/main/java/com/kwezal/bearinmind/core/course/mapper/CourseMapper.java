package com.kwezal.bearinmind.core.course.mapper;

import com.kwezal.bearinmind.core.course.dto.*;
import com.kwezal.bearinmind.core.course.model.Course;
import com.kwezal.bearinmind.core.course.view.CourseListItemView;
import com.kwezal.bearinmind.core.course.view.UserCourseView;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

// FIXME Explicitly defining the OffsetDateTime import may be redundant in the future, but due to mapstruct bug it is necessary now
@Mapper(imports = OffsetDateTime.class)
public interface CourseMapper {
    Course map(CourseDto dto);

    @Mapping(target = "creationDateTime", expression = "java( OffsetDateTime.now() )")
    @Mapping(target = "lessons", ignore = true)
    Course map(CreateCourseDto dto);

    void update(@MappingTarget Course entity, UpdateCourseDto dto, Integer descriptionIdentifier);

    CourseListItemDto mapToCourseListItemDto(CourseListItemView view, String name);

    default List<CourseListItemDto> mapToCourseListItemDtos(List<CourseListItemView> views, Map<Integer, String> translations) {
        return views
            .stream()
            .map(projection -> mapToCourseListItemDto(projection, translations.get(projection.getNameIdentifier())))
            .toList();
    }

    default CourseMainViewDto mapToCourseMainViewDto(
        List<CourseListItemView> activeCourses,
        List<CourseListItemView> availableCourses,
        List<CourseListItemView> completedCourses,
        Map<Integer, String> translations
    ) {
        return new CourseMainViewDto(
            mapToCourseListItemDtos(activeCourses, translations),
            mapToCourseListItemDtos(availableCourses, translations),
            mapToCourseListItemDtos(completedCourses, translations)
        );
    }

    UserCourseDto mapToUserCourseDto(UserCourseView view, String name);

    default List<UserCourseDto> mapToUserCourseDtos(List<UserCourseView> views, Map<Integer, String> translations) {
        return views.stream().map(view -> mapToUserCourseDto(view, translations.get(view.getNameIdentifier()))).toList();
    }
}
