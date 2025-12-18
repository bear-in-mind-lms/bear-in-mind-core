package com.kwezal.bearinmind.core.user.repository;

import com.kwezal.bearinmind.core.user.model.UserGroup;
import com.kwezal.bearinmind.core.user.view.UserGroupListItemView;
import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserGroupRepository extends JpaRepository<@NonNull UserGroup, @NonNull Long> {
    /**
     * Finds a page of user groups in which a given user is registered.
     * A user is registered in a group if they have the owner or member role.
     *
     * @param userId   user ID
     * @param pageable pagination information
     * @return user group page
     */
    @Query(
        """
                    SELECT ug.id AS id, ug.nameIdentifier AS nameIdentifier, ug.image AS image
                    FROM UserGroup ug
                    JOIN UserGroupMember ugm ON (ugm.group = ug)
                    WHERE ugm.user.id = :userId
                    AND ugm.role IN (com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.OWNER, com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.MEMBER)
                    GROUP BY ug.id, ugm.registrationDateTime
                    ORDER BY ugm.registrationDateTime DESC"""
    )
    Page<@NonNull UserGroupListItemView> findAllRegisteredUserGroupListItemByUserId(Long userId, Pageable pageable);

    /**
     * Finds a page of available user groups for a given user.
     * A user group is available if the user is not registered in it yet.
     *
     * @param userId   user ID
     * @param pageable pagination information
     * @return user group page
     */
    @Query(
        """
                    SELECT ug.id AS id, ug.nameIdentifier AS nameIdentifier, ug.image AS image
                    FROM UserGroup ug
                    LEFT JOIN UserGroupMember ugm ON (ugm.group = ug AND ugm.user.id = :userId)
                    WHERE ugm.user.id IS NULL
                    GROUP BY ug.id
                    ORDER BY ug.creationDateTime DESC"""
    )
    Page<@NonNull UserGroupListItemView> findAllAvailableUserGroupListItemByUserId(Long userId, Pageable pageable);

    /**
     * Finds a user group and its members for a given group.
     * A member of a group is a user who has the owner or member role.
     *
     * @param groupId group ID
     * @return user group
     */
    @Query(
        """
                    SELECT ug
                    FROM UserGroup ug
                    JOIN ug.members ugm
                    WHERE ug.id = :groupId
                    AND ugm.role IN (com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.OWNER, com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.MEMBER)
                    """
    )
    UserGroup findUserGroupWithMembersById(Long groupId);

    /**
     * Finds a list of user groups which both users belong to.
     *
     * @param loggedInUserId logged-in user ID
     * @param userId         user ID
     * @return user group list
     */
    @Query(
        """
                    SELECT ug.id AS id, ug.nameIdentifier AS nameIdentifier, ug.image AS image
                    FROM UserGroup ug
                    JOIN UserGroupMember ugml ON (ugml.group = ug AND ugml.user.id = :loggedInUserId)
                    JOIN UserGroupMember ugmu ON (ugmu.group = ug AND ugmu.user.id = :userId)
                    WHERE ugml.role IN (com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.OWNER, com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.MEMBER)
                    AND ugmu.role IN (com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.OWNER, com.kwezal.bearinmind.core.user.enumeration.UserGroupRole.MEMBER)
                    GROUP BY ug.id"""
    )
    List<UserGroupListItemView> findAllCommonUserGroup(Long loggedInUserId, Long userId);
}
