package com.kwezal.bearinmind.core.course.repository;

import com.kwezal.bearinmind.core.course.model.Course;
import com.kwezal.bearinmind.core.course.view.CourseListItemView;
import com.kwezal.bearinmind.core.course.view.UserCourseView;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CourseRepository extends JpaRepository<@NonNull Course, @NonNull Long> {
    /**
     * Finds a page of conducted courses for a given user.
     * A conducted course is one that has not ended and that user has the owner or teacher role in.
     *
     * @param userId   user ID
     * @param pageable pagination information
     * @return course page
     */
    @Query(
        """
                    SELECT c.id AS id, c.nameIdentifier AS nameIdentifier, c.image AS image
                    FROM Course c
                    JOIN CourseUserData cud ON (cud.course = c)
                    WHERE cud.user.id = :userId AND
                        (c.endDateTime IS NULL OR c.endDateTime > CURRENT_TIMESTAMP) AND
                        cud.role IN (com.kwezal.bearinmind.core.course.enumeration.CourseRole.OWNER, com.kwezal.bearinmind.core.course.enumeration.CourseRole.TEACHER)
                    GROUP BY c.id, cud.lastAccessDateTime
                    ORDER BY cud.lastAccessDateTime DESC"""
    )
    Page<CourseListItemView> findAllConductedCourseListItemByUserId(Long userId, Pageable pageable);

    /**
     * Finds a page of active courses for a given user.
     * An active course is one that has not ended and that user is enrolled in.
     *
     * @param userId   user ID
     * @param pageable pagination information
     * @return course page
     */
    @Query(
        """
                    SELECT c.id AS id, c.nameIdentifier AS nameIdentifier, c.image AS image
                    FROM Course c
                    JOIN CourseUserData cud ON (cud.course = c)
                    WHERE cud.user.id = :userId AND
                        (c.endDateTime IS NULL OR c.endDateTime > CURRENT_TIMESTAMP) AND
                        cud.role = com.kwezal.bearinmind.core.course.enumeration.CourseRole.STUDENT
                    GROUP BY c.id, cud.lastAccessDateTime
                    ORDER BY cud.lastAccessDateTime DESC"""
    )
    Page<CourseListItemView> findAllActiveCourseListItemByUserId(Long userId, Pageable pageable);

    /**
     * Finds a page of available courses for a given user.
     * Course availability is defined by belonging to a user group assigned to a given, active course.
     *
     * @param userId   user ID
     * @param pageable pagination information
     * @return course page
     */
    @Query(
        """
                    SELECT c.id AS id, c.nameIdentifier AS nameIdentifier, c.image AS image
                    FROM Course c
                    LEFT JOIN CourseUserData cud ON (cud.course = c AND cud.user.id = :userId)
                    JOIN CourseUserGroup cug ON (cug.course = c)
                    JOIN UserGroupMember cgm ON (cgm.group = cug.group AND cgm.user.id = :userId)
                    WHERE cud.user.id IS NULL AND
                        (c.endDateTime IS NULL OR c.endDateTime > CURRENT_TIMESTAMP) AND
                        cgm.role IN (com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.OWNER, com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.MEMBER)
                    GROUP BY c.id
                    ORDER BY c.creationDateTime DESC"""
    )
    Page<CourseListItemView> findAllAvailableCourseListItemByUserId(Long userId, Pageable pageable);

    /**
     * Finds a page of completed courses for a given user.
     * A completed course is one that has ended and that user was enrolled in or conducted it.
     *
     * @param userId   user ID
     * @param pageable pagination information
     * @return course page
     */
    @Query(
        """
                    SELECT c.id AS id, c.nameIdentifier AS nameIdentifier, c.image AS image
                    FROM Course c
                    JOIN CourseUserData cud ON (cud.course = c)
                    WHERE cud.user.id = :userId AND c.endDateTime <= CURRENT_TIMESTAMP
                    GROUP BY c.id
                    ORDER BY c.endDateTime DESC"""
    )
    Page<CourseListItemView> findAllCompletedCourseListItemByUserId(Long userId, Pageable pageable);

    /**
     * Finds a list of courses which both users are enrolled in.
     * Retrieves additional information about the role of user {@code userId} in the course.
     *
     * @param loggedInUserId logged-in user ID
     * @param userId         user ID
     * @return list of courses with roles
     */
    @Query(
        """
                    SELECT c.id AS id, c.nameIdentifier AS nameIdentifier, c.image AS image, cudu.role AS role
                    FROM Course c
                    JOIN CourseUserData cudl ON (cudl.course = c AND cudl.user.id = :loggedInUserId)
                    JOIN CourseUserData cudu ON (cudu.course = c AND cudu.user.id = :userId)
                    GROUP BY c.id, cudu.role"""
    )
    List<UserCourseView> findAllCommonCourseAndRole(Long loggedInUserId, Long userId);
}
