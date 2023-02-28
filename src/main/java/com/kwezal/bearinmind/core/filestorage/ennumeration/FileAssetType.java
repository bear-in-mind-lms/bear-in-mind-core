package com.kwezal.bearinmind.core.filestorage.ennumeration;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.kwezal.bearinmind.core.utils.EnumUtils;

public enum FileAssetType {
    USER,
    USER_GROUP,
    COURSE,
    COURSE_LESSON;

    @JsonCreator
    public static FileAssetType fromNameOrNull(final String name) {
        return EnumUtils.fromNameOrNull(FileAssetType.class, name);
    }
}
