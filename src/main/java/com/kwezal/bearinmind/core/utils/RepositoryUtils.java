package com.kwezal.bearinmind.core.utils;

import com.kwezal.bearinmind.exception.ResourceNotFoundException;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RepositoryUtils {

    public static <T, ID> T fetch(
        final ID id,
        final CrudRepository<@NonNull T, @NonNull ID> repository,
        final Class<T> entityClass
    ) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException(entityClass, Map.of("id", id)));
    }
}
