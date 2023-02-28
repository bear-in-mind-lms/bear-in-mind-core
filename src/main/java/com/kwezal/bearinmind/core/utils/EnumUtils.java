package com.kwezal.bearinmind.core.utils;

import static java.util.Objects.isNull;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EnumUtils {

    public static <T extends Enum<T>> T fromNameOrNull(final Class<T> enumClass, final String name) {
        if (isNull(name)) {
            return null;
        }

        try {
            return Enum.valueOf(enumClass, name);
        } catch (Exception e) {
            return null;
        }
    }
}
