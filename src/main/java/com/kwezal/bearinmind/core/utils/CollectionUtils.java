package com.kwezal.bearinmind.core.utils;

import static java.util.Objects.nonNull;

import jakarta.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CollectionUtils {

    public static <T> boolean addIgnoreNull(final Collection<T> collection, final @Nullable T object) {
        if (nonNull(object)) {
            collection.add(object);
            return true;
        }
        return false;
    }

    public static <K1, K2, V> Map<K2, Map<K1, V>> swapMapKeys(final Map<K1, Map<K2, V>> mapOfMaps) {
        final var result = new HashMap<K2, Map<K1, V>>();

        mapOfMaps.forEach((k1, k2Map) ->
            k2Map.forEach((k2, value) -> {
                result.computeIfAbsent(k2, k -> new HashMap<>()).put(k1, value);
            })
        );

        return result;
    }
}
