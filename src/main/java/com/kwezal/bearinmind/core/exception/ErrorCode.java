package com.kwezal.bearinmind.core.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ErrorCode {

    // BAD REQUEST
    public static final String REQUEST_ARGUMENT_INVALID =
        com.kwezal.bearinmind.exception.response.ErrorCode.REQUEST_ARGUMENT_INVALID;
    public static final String CANNOT_ENROLL = "CANNOT_ENROLL";
    public static final String CANNOT_JOIN_GROUP = "CANNOT_JOIN_GROUP";
    public static final String FILE_SIZE_LIMIT_EXCEEDED = "FILE_SIZE_LIMIT_EXCEEDED";
    public static final String USER_EXISTS = "USER_EXISTS";

    public static final String INVALID_COURSE_START_DATE_TIME_IS_AFTER_END_DATE_TIME =
        "INVALID_COURSE_START_DATE_TIME_IS_AFTER_END_DATE_TIME";
    public static final String INVALID_COURSE_REGISTRATION_CLOSING_DATE_TIME_IS_AFTER_END_DATE_TIME =
        "INVALID_COURSE_REGISTRATION_CLOSING_DATE_TIME_IS_AFTER_END_DATE_TIME";
    public static final String INVALID_COURSE_END_DATE_TIME_IS_BEFORE_NOW_TIME =
        "INVALID_COURSE_END_DATE_TIME_IS_BEFORE_NOW_TIME";
    public static final String INVALID_COURSE_REGISTRATION_CLOSING_DATE_TIME_IS_BEFORE_NOW_TIME =
        "INVALID_COURSE_REGISTRATION_CLOSING_DATE_TIME_IS_BEFORE_NOW_TIME";
    public static final String INVALID_COURSE_START_DATE_TIME_IS_AFTER_REGISTRATION_CLOSING_DATE_TIME =
        "INVALID_COURSE_START_DATE_TIME_IS_AFTER_REGISTRATION_CLOSING_DATE_TIME";
    public static final String INVALID_COURSE_LESSON_START_DATE_TIME_OR_END_DATE_TIME =
        "INVALID_COURSE_LESSON_START_DATE_TIME_OR_END_DATE_TIME";
    public static final String INVALID_COURSE_LESSON_PART_ATTACHMENT_OR_TRANSLATIONS =
        "INVALID_COURSE_LESSON_PART_ATTACHMENT_OR_TRANSLATIONS";

    // AUTHORIZATION
    public static final String INCORRECT_CREDENTIALS = "INCORRECT_CREDENTIALS";

    // FORBIDDEN
    public static final String FORBIDDEN = com.kwezal.bearinmind.exception.response.ErrorCode.FORBIDDEN;
    public static final String NO_ACCESS_TO_LESSON = "NO_ACCESS_TO_LESSON";
}
