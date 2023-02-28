package com.kwezal.bearinmind.core.course.mapper;

import com.kwezal.bearinmind.core.course.dto.CourseUserDataDto;
import com.kwezal.bearinmind.core.course.dto.CreateOrUpdateCourseUserDataDto;
import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import com.kwezal.bearinmind.core.course.model.Course;
import com.kwezal.bearinmind.core.course.model.CourseUserData;
import com.kwezal.bearinmind.core.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface CourseUserDataMapper {
    CourseUserData map(CourseUserDataDto dto);

    @Mapping(target = "courseId", source = "course.id")
    @Mapping(target = "userId", source = "user.id")
    CourseUserDataDto mapToCourseUserDataDto(CourseUserData entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "lastAccessDateTime", expression = "java( OffsetDateTime.now() )")
    CourseUserData map(Course course, User user, CourseRole role);

    @Mapping(target = "lastAccessDateTime", expression = "java( OffsetDateTime.now() )")
    CourseUserData map(CreateOrUpdateCourseUserDataDto dto);

    void update(@MappingTarget CourseUserData entity, CreateOrUpdateCourseUserDataDto dto);
}
