package com.kwezal.bearinmind.core.user;

import static com.kwezal.bearinmind.core.exception.ErrorCode.CANNOT_JOIN_GROUP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.kwezal.bearinmind.core.ControllerTest;
import com.kwezal.bearinmind.core.user.enumeration.UserGroupRole;
import com.kwezal.bearinmind.core.user.repository.UserGroupMemberRepository;
import com.kwezal.bearinmind.core.utils.AuthHelper;
import com.kwezal.bearinmind.exception.response.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(scripts = "/db/cleanup/USER_GROUP_MEMBER.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
class UserGroupMemberControllerTest extends ControllerTest {

    @Override
    public String urlBase() {
        return "/user/group";
    }

    @Autowired
    private UserGroupMemberRepository userGroupMemberRepository;

    @Autowired
    private AuthHelper authHelper;

    @Test
    void Should_AddUserToUserGroup_When_CorrectRequest() {
        // GIVEN
        final var groupId = 3L;
        final var userId = AuthHelper.DEFAULT_USER_ID;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.post().uri(builder -> url(builder, "/join/{groupId}").build(groupId)))
            .exchange();

        // THEN
        response.expectStatus().isNoContent();

        // AND
        final var optionalUserGroupMember = userGroupMemberRepository.findByGroupIdAndUserId(groupId, userId);
        assertTrue(optionalUserGroupMember.isPresent());

        final var userGroupMember = optionalUserGroupMember.get();
        assertEquals(UserGroupRole.MEMBER, userGroupMember.getRole());
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToAddGroupMemberToUserGroup() {
        // GIVEN
        final var groupId = 1L;

        final var expectedErrorCode = CANNOT_JOIN_GROUP;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.post().uri(builder -> url(builder, "/join/{groupId}").build(groupId)))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();

        // AND
        response.expectBody(ErrorResponse.class).value(responseDto -> assertEquals(expectedErrorCode, responseDto.code()));
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToAddUserToUserGroupWithIncorrectGroupId() {
        // GIVEN
        final var groupId = 0L;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.post().uri(builder -> url(builder, "/join/{groupId}").build(groupId)))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }
}
