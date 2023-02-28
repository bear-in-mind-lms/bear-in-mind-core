package com.kwezal.bearinmind.core.course.view;

import com.kwezal.bearinmind.core.course.enumeration.CourseRole;

public interface UserCourseView {
    Long getId();

    Integer getNameIdentifier();

    String getImage();

    CourseRole getRole();
}
