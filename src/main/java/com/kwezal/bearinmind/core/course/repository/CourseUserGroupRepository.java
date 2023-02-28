package com.kwezal.bearinmind.core.course.repository;

import com.kwezal.bearinmind.core.course.model.CourseUserGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseUserGroupRepository extends JpaRepository<CourseUserGroup, Long> {
    /**
     * Checks if a given user is a member of one of the user groups associated with a given course.
     *
     * @param courseId course ID
     * @param userId   user ID
     * @return <code>true</code> if the user is a member of the group associated with the course,
     * <code>false</code> otherwise
     */
    @Query(
        """
                    SELECT CASE
                        WHEN COUNT(cug.id) > 0 THEN true
                        ELSE false
                    END
                    FROM CourseUserGroup cug
                    JOIN UserGroupMember cgm ON (cgm.group = cug.group AND cgm.user.id = :userId)
                    WHERE cug.course.id = :courseId AND
                        cgm.role IN (com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.OWNER, com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.MEMBER)
                    """
    )
    boolean existsByCourseIdAndUserId(Long courseId, Long userId);
}
