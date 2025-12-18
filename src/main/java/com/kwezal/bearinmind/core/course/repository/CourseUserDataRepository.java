package com.kwezal.bearinmind.core.course.repository;

import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import com.kwezal.bearinmind.core.course.model.CourseUserData;
import com.kwezal.bearinmind.core.user.dto.UserListItemDto;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseUserDataRepository extends JpaRepository<@NonNull CourseUserData, @NonNull Long> {
    boolean existsByCourseIdAndUserId(Long courseId, Long userId);

    boolean existsByUserIdAndRole(Long userId, CourseRole role);

    boolean existsByUserIdAndRoleIn(Long userId, EnumSet<CourseRole> role);

    boolean existsByCourseIdAndUserIdAndRole(Long courseId, Long userId, CourseRole role);

    boolean existsByCourseIdAndUserIdAndRoleIn(Long courseId, Long userId, EnumSet<CourseRole> role);

    /**
     * Finds the role that a given user has in a given course.
     *
     * @param courseId course ID
     * @param userId   user ID
     * @return course role
     */
    @Query(
        """
                    SELECT cud.role
                    FROM CourseUserData cud
                    WHERE cud.course.id = :courseId AND cud.user.id = :userId"""
    )
    Optional<CourseRole> findCourseRoleByCourseIdAndUserId(Long courseId, Long userId);

    /**
     * Finds the role that a given user has in a course with a given lesson.
     *
     * @param lessonId lesson ID
     * @param userId   user ID
     * @return course role
     */
    @Query(
        """
                    SELECT cud.role
                    FROM CourseUserData cud
                    JOIN CourseLesson cl ON (cl.course = cud.course)
                    WHERE cl.id = :lessonId AND cud.user.id = :userId"""
    )
    Optional<CourseRole> findCourseRoleByCourseLessonIdAndUserId(Long lessonId, Long userId);

    /**
     * Finds a list of teachers in a given course.
     * A teacher is a user who has the owner or teacher role.
     *
     * @param courseId course ID
     * @return list of users
     */
    @Query(
        """
                    SELECT new com.kwezal.bearinmind.core.user.dto.UserListItemDto(u.id, CONCAT(u.firstName, ' ', u.lastName), u.image)
                    FROM Course c
                    JOIN CourseUserData cud ON (cud.course = c)
                    JOIN User u ON (u.id = cud.user.id)
                    WHERE c.id = :courseId
                    AND cud.role IN(com.kwezal.bearinmind.core.course.enumeration.CourseRole.OWNER, com.kwezal.bearinmind.core.course.enumeration.CourseRole.TEACHER)"""
    )
    List<UserListItemDto> findAllTeacherByCourseId(Long courseId);

    /**
     * Finds the ID of the course user data for a given user with a given role in a course with a given lesson.
     *
     * @param lessonId lesson ID
     * @param userId   user ID
     * @param role     course role
     * @return course user data ID
     */
    @Query(
        """
                    SELECT cud.id
                    FROM CourseUserData cud
                    JOIN CourseLesson cl ON (cl.course = cud.course)
                    WHERE cl.id = :lessonId AND cud.user.id = :userId AND cud.role = :role
                    """
    )
    Optional<Long> findIdByCourseLessonIdAndUserIdAndRole(Long lessonId, Long userId, CourseRole role);

    default boolean existsByCourseLessonIdAndUserIdAndRole(Long lessonId, Long userId, CourseRole role) {
        return findIdByCourseLessonIdAndUserIdAndRole(lessonId, userId, role).isPresent();
    }
}
