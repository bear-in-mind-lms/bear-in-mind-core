package com.kwezal.bearinmind.core.course;

import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import com.kwezal.bearinmind.core.ControllerSecurityTest;
import com.kwezal.bearinmind.core.course.dto.CreateCourseLessonDto;
import com.kwezal.bearinmind.core.course.dto.UpdateCourseLessonDto;
import com.kwezal.bearinmind.core.user.dto.UserRole;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CourseLessonControllerSecurityTest extends ControllerSecurityTest {

    @Override
    public String urlBase() {
        return "/course";
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnUnauthorized_When_DoesNotHaveRequiredRole_Source")
    void Should_ReturnUnauthorized_When_DoesNotHaveRequiredRole(HttpMethod method, String path, UserRole role, Object body) {
        // WHEN
        final var response = request(method, path, role, body);

        // THEN
        response.expectStatus().isUnauthorized();
    }

    private static Stream<Arguments> Should_ReturnUnauthorized_When_DoesNotHaveRequiredRole_Source() {
        return Stream.of(
            Arguments.of(
                POST,
                "/1/lesson",
                UserRole.STUDENT_ROLE_GROUP,
                new CreateCourseLessonDto(Map.of("en", Collections.emptyMap()), null, null)
            ),
            Arguments.of(PUT, "/lesson/1", UserRole.STUDENT_ROLE_GROUP, new UpdateCourseLessonDto(Collections.emptyMap(), null))
        );
    }
}
