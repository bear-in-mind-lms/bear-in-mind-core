package com.kwezal.bearinmind.core.course.enumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.kwezal.bearinmind.core.utils.EnumUtils;

public enum CourseRole {
    OWNER,
    TEACHER,
    STUDENT;

    @JsonCreator
    public static CourseRole fromNameOrNull(final String name) {
        return EnumUtils.fromNameOrNull(CourseRole.class, name);
    }

    public boolean isTeacher() {
        return this == OWNER || this == TEACHER;
    }
}
