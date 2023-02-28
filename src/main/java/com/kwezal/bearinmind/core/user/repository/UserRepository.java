package com.kwezal.bearinmind.core.user.repository;

import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import com.kwezal.bearinmind.core.user.dto.UserListItemDto;
import com.kwezal.bearinmind.core.user.model.User;
import java.util.EnumSet;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    Optional<User> findByUserCredentialsUsernameAndUserCredentialsActiveTrue(String username);

    boolean existsByUserCredentialsUsernameOrEmailAndUserCredentialsActiveTrue(String username, String email);

    /**
     * Finds a page of users who are members of at least one common group with a given user.
     *
     * @param userId   user ID
     * @param pageable pagination information
     * @return user page
     */
    @Query(
        """
                    SELECT new com.kwezal.bearinmind.core.user.dto.UserListItemDto(u.id, CONCAT(u.firstName, ' ', u.lastName), u.image)
                    FROM User u
                    JOIN UserGroupMember ugmm ON (ugmm.user = u)
                    JOIN UserGroupMember ugmu ON (ugmu.group = ugmm.group AND ugmu.user.id = :userId)
                    WHERE u.id <> :userId
                    AND ugmm.role IN (com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.OWNER, com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.MEMBER)
                    AND ugmu.role IN (com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.OWNER, com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.MEMBER)
                    GROUP BY u.id
                    """
    )
    Page<UserListItemDto> findAllGroupMemberUserListItemByUserId(Long userId, Pageable pageable);

    /**
     * Finds a page of users who have given roles in at least one course where a given user has given roles.
     *
     * @param userId            user ID
     * @param userRoles         roles of the user with a given ID in a course
     * @param searchedUserRoles roles of searched users in a course
     * @param pageable          pagination information
     * @return user page
     */
    @Query(
        """
                    SELECT new com.kwezal.bearinmind.core.user.dto.UserListItemDto(u.id, CONCAT(u.firstName, ' ', u.lastName), u.image)
                    FROM User u
                    JOIN CourseUserData cuds ON (cuds.user = u)
                    JOIN CourseUserData cudu ON (cudu.course = cuds.course AND cudu.user.id = :userId)
                    WHERE u.id <> :userId
                    AND cuds.role IN :searchedUserRoles
                    AND cudu.role IN :userRoles
                    GROUP BY u.id
                    """
    )
    Page<UserListItemDto> findAllUserListItemByUserIdAndCourseRoleIn(
        Long userId,
        EnumSet<CourseRole> userRoles,
        EnumSet<CourseRole> searchedUserRoles,
        Pageable pageable
    );
}
