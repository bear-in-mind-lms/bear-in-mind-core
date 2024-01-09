package com.kwezal.bearinmind.core.user;

import static com.kwezal.bearinmind.core.utils.AssertionUtils.assertEqualsIgnoringOrder;
import static com.kwezal.bearinmind.core.utils.RepositoryUtils.fetch;
import static com.kwezal.bearinmind.core.utils.TestConstants.ID_SEQUENCE_START;
import static com.kwezal.bearinmind.core.utils.TestConstants.NONEXISTENT_ID;
import static org.junit.jupiter.api.Assertions.*;

import com.kwezal.bearinmind.core.ControllerTestInterface;
import com.kwezal.bearinmind.core.user.dto.CreateOrUpdateUserGroupDto;
import com.kwezal.bearinmind.core.user.dto.UserGroupDto;
import com.kwezal.bearinmind.core.user.dto.UserGroupListItemDto;
import com.kwezal.bearinmind.core.user.dto.UserListItemDto;
import com.kwezal.bearinmind.core.user.enumeration.UserGroupRole;
import com.kwezal.bearinmind.core.user.model.User;
import com.kwezal.bearinmind.core.user.model.UserGroup;
import com.kwezal.bearinmind.core.user.model.UserGroupMember;
import com.kwezal.bearinmind.core.user.repository.UserGroupMemberRepository;
import com.kwezal.bearinmind.core.user.repository.UserGroupRepository;
import com.kwezal.bearinmind.core.utils.AuthHelper;
import com.kwezal.bearinmind.core.utils.Page;
import com.kwezal.bearinmind.translation.model.Translation;
import com.kwezal.bearinmind.translation.repository.TranslationRepository;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Sql(
    scripts = { "/db/cleanup/USER_GROUP_MEMBER.sql", "/db/cleanup/USER_GROUP.sql", "/db/cleanup/TRANSLATION.sql" },
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
@SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
class UserGroupControllerTest implements ControllerTestInterface {

    @Override
    public String urlBase() {
        return "/user/group";
    }

    @Value("${application.locale}")
    String applicationLocale;

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private AuthHelper authHelper;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private UserGroupRepository userGroupRepository;

    @Autowired
    private UserGroupMemberRepository userGroupMemberRepository;

    @Autowired
    private TranslationRepository translationRepository;

    @Transactional
    @Test
    void Should_CreateUserGroup_When_CorrectRequest() {
        // GIVEN
        final var expectedName = "The Illuminati";
        final var name = Map.of(applicationLocale, expectedName);
        final var dto = new CreateOrUpdateUserGroupDto(name);

        final var expectedUserGroupId = ID_SEQUENCE_START;
        final var expectedMemberIdRoleMap = Map.of(AuthHelper.DEFAULT_USER_ID, UserGroupRole.OWNER);

        // WHEN
        final var response = authHelper
            .asStudent(webClient.post().uri(url()).body(Mono.just(dto), CreateOrUpdateUserGroupDto.class))
            .exchange();

        // THEN
        response.expectStatus().isCreated();

        // AND
        response.expectBody(Long.class).value(id -> assertEquals(expectedUserGroupId, id));

        // AND
        final var userGroup = fetch(expectedUserGroupId, userGroupRepository, UserGroup.class);

        final var optionalNameTranslation = translationRepository.findByIdentifierAndLocale(
            userGroup.getNameIdentifier(),
            applicationLocale
        );
        assertTrue(optionalNameTranslation.isPresent());
        final var nameTranslation = optionalNameTranslation.get();

        assertEquals(expectedName, nameTranslation.getText());
        assertNull(userGroup.getImage());
        assertEquals(
            expectedMemberIdRoleMap,
            userGroup
                .getMembers()
                .stream()
                .collect(Collectors.toMap(member -> member.getUser().getId(), UserGroupMember::getRole))
        );
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToCreateUserGroupWithoutBody() {
        // WHEN
        final var response = authHelper.asStudent(webClient.post().uri(url())).exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnBadRequest_When_AttemptToCreateUserGroupWithIncorrectData_Source")
    void Should_ReturnBadRequest_When_AttemptToCreateUserGroupWithIncorrectData(Map<String, String> name) {
        // GIVEN
        final var dto = new CreateOrUpdateUserGroupDto(name);

        // WHEN
        final var response = authHelper
            .asStudent(webClient.post().uri(url()).body(Mono.just(dto), CreateOrUpdateUserGroupDto.class))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    private static Stream<Arguments> Should_ReturnBadRequest_When_AttemptToCreateUserGroupWithIncorrectData_Source() {
        return Stream.of(Arguments.of(Collections.emptyMap()), Arguments.of(Map.of("da", "Illuminati-ordenen")));
    }

    @Test
    void Should_UpdateUserGroup_When_CorrectRequest() {
        // GIVEN
        final var existingUserGroup = createUserGroup("The Illuminati");
        final var existingUserGroupId = existingUserGroup.getId();

        final var expectedName = "Nothing to see here";
        final var name = Map.of(applicationLocale, expectedName);
        final var dto = new CreateOrUpdateUserGroupDto(name);

        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .put()
                    .uri(builder -> url(builder, "/{groupId}").build(existingUserGroupId))
                    .body(Mono.just(dto), CreateOrUpdateUserGroupDto.class)
            )
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        final var userGroup = fetch(existingUserGroupId, userGroupRepository, UserGroup.class);

        final var optionalNameTranslation = translationRepository.findByIdentifierAndLocale(
            userGroup.getNameIdentifier(),
            applicationLocale
        );
        assertTrue(optionalNameTranslation.isPresent());
        final var nameTranslation = optionalNameTranslation.get();

        assertEquals(expectedName, nameTranslation.getText());
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToUpdateUserGroupWithoutBody() {
        // GIVEN
        final var groupId = 1L;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.put().uri(builder -> url(builder, "/{groupId}").build(groupId)))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToUpdateUserGroupWithIncorrectData() {
        // GIVEN
        final var groupId = 1L;
        final var dto = new CreateOrUpdateUserGroupDto(Collections.emptyMap());

        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .put()
                    .uri(builder -> url(builder, "/{groupId}").build(groupId))
                    .body(Mono.just(dto), CreateOrUpdateUserGroupDto.class)
            )
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @Test
    void Should_ReturnUserGroupPage_When_CorrectRequest() {
        // GIVEN
        final var groupId = 2L;
        final var expectedGroupName = "Teachers";
        final var expectedUserIds = List.of(1L, 2L, 3L, 4L, 5L);

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/{groupId}").build(groupId)))
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(UserGroupDto.class)
            .value(responseDto -> {
                assertEquals(expectedGroupName, responseDto.getName());
                assertEqualsIgnoringOrder(expectedUserIds, responseDto.getMembers().stream().map(UserListItemDto::id).toList());
            });
    }

    @Test
    void Should_ReturnBadRequest_When_AttemptToGetGroupPageWithIncorrectId() {
        // GIVEN
        final var groupId = 0L;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/{groupId}").build(groupId)))
            .exchange();

        // THEN
        response.expectStatus().isBadRequest();
    }

    @Test
    void Should_ReturnNotFound_When_RequestedUserGroupDoesNotExist() {
        // GIVEN
        final var groupId = NONEXISTENT_ID;

        // WHEN
        final var response = authHelper
            .asStudent(webClient.get().uri(builder -> url(builder, "/{groupId}").build(groupId)))
            .exchange();

        // THEN
        response.expectStatus().isNotFound();
    }

    @Test
    void Should_ReturnRegisteredUserGroupPage_When_CorrectRequest() {
        // GIVEN
        final var pageNumber = 0;
        final var pageSize = 5;
        final var expectedGroupIds = List.of(1L, 2L);

        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .get()
                    .uri(builder ->
                        url(builder, "/list/registered")
                            .queryParam("pageNumber", pageNumber)
                            .queryParam("pageSize", pageSize)
                            .build()
                    )
            )
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(new ParameterizedTypeReference<Page<UserGroupListItemDto>>() {})
            .value(responseDto -> {
                assertEquals(pageNumber, responseDto.getNumber());
                assertEqualsIgnoringOrder(
                    expectedGroupIds,
                    responseDto.getContent().stream().map(UserGroupListItemDto::id).toList()
                );
            });
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnBadRequest_When_AttemptToGetRegisteredUserGroupPageWithIncorrectData_Source")
    void Should_ReturnBadRequest_When_AttemptToGetRegisteredUserGroupPageWithIncorrectData(int pageNumber, int pageSize) {
        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .get()
                    .uri(builder ->
                        url(builder, "/list/registered")
                            .queryParam("pageNumber", pageNumber)
                            .queryParam("pageSize", pageSize)
                            .build()
                    )
            )
            .exchange();
        // THEN
        response.expectStatus().isBadRequest();
    }

    private static Stream<Arguments> Should_ReturnBadRequest_When_AttemptToGetRegisteredUserGroupPageWithIncorrectData_Source() {
        return Stream.of(Arguments.of(-1, 1), Arguments.of(1, 0), Arguments.of(1, 101));
    }

    @Test
    void Should_ReturnAvailableUserGroupPage_When_CorrectRequest() {
        // GIVEN
        final var pageNumber = 0;
        final var pageSize = 5;
        final var expectedGroupIds = List.of(3L);

        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .get()
                    .uri(builder ->
                        url(builder, "/list/available")
                            .queryParam("pageNumber", pageNumber)
                            .queryParam("pageSize", pageSize)
                            .build()
                    )
            )
            .exchange();

        // THEN
        response.expectStatus().is2xxSuccessful();

        // AND
        response
            .expectBody(new ParameterizedTypeReference<Page<UserGroupListItemDto>>() {})
            .value(responseDto -> {
                assertEquals(pageNumber, responseDto.getNumber());
                assertEqualsIgnoringOrder(
                    expectedGroupIds,
                    responseDto.getContent().stream().map(UserGroupListItemDto::id).toList()
                );
            });
    }

    @ParameterizedTest
    @MethodSource("Should_ReturnBadRequest_When_AttemptToGetAvailableUserGroupPageWithIncorrectData_Source")
    void Should_ReturnBadRequest_When_AttemptToGetAvailableUserGroupPageWithIncorrectData(int pageNumber, int pageSize) {
        // WHEN
        final var response = authHelper
            .asStudent(
                webClient
                    .get()
                    .uri(builder ->
                        url(builder, "/list/available")
                            .queryParam("pageNumber", pageNumber)
                            .queryParam("pageSize", pageSize)
                            .build()
                    )
            )
            .exchange();
        // THEN
        response.expectStatus().isBadRequest();
    }

    private static Stream<Arguments> Should_ReturnBadRequest_When_AttemptToGetAvailableUserGroupPageWithIncorrectData_Source() {
        return Stream.of(Arguments.of(-1, 1), Arguments.of(1, 0), Arguments.of(1, 101));
    }

    private UserGroup createUserGroup(String name) {
        final var nameTranslation = translationRepository.save(new Translation(null, null, applicationLocale, name));
        final var userGroup = userGroupRepository.save(
            new UserGroup(null, nameTranslation.getIdentifier(), null, OffsetDateTime.now(), null)
        );
        userGroupMemberRepository.save(
            new UserGroupMember(
                null,
                userGroup,
                entityManager.getReference(User.class, AuthHelper.DEFAULT_USER_ID),
                UserGroupRole.OWNER,
                OffsetDateTime.now()
            )
        );
        return userGroup;
    }
}
