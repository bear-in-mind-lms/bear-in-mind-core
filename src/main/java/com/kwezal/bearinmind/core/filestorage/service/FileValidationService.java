package com.kwezal.bearinmind.core.filestorage.service;

import static com.kwezal.bearinmind.core.exceptions.ErrorCode.FILE_SIZE_LIMIT_EXCEEDED;

import com.kwezal.bearinmind.core.course.enumeration.CourseRole;
import com.kwezal.bearinmind.core.course.repository.CourseUserDataRepository;
import com.kwezal.bearinmind.core.exceptions.ForbiddenException;
import com.kwezal.bearinmind.core.exceptions.InvalidRequestDataException;
import com.kwezal.bearinmind.core.filestorage.ennumeration.FileAssetType;
import com.kwezal.bearinmind.core.user.enumeration.UserGroupRole;
import com.kwezal.bearinmind.core.user.repository.UserGroupMemberRepository;
import com.kwezal.bearinmind.filestorage.model.ImageExtension;
import com.kwezal.bearinmind.filestorage.model.ImageLimitSize;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
class FileValidationService {

    private static final EnumMap<FileAssetType, ImageLimitSize> IMAGE_ASSET_TYPE_IMAGE_LIMIT_SIZE = new EnumMap<>(
        Map.of(
            FileAssetType.USER,
            ImageLimitSize.SMALL,
            FileAssetType.COURSE,
            ImageLimitSize.MEDIUM,
            FileAssetType.COURSE_LESSON,
            ImageLimitSize.MEDIUM
        )
    );

    private final CourseUserDataRepository courseUserDataRepository;
    private final UserGroupMemberRepository userGroupMemberRepository;

    /**
     * Throws exception if a given image exceeds the size limit.
     *
     * @param multipartFile image file
     * @param fileAssetType asset type
     * @param identifier    asset identifier
     * @throws InvalidRequestDataException if validation fails
     */
    void validateImageDoesNotExceedSizeLimit(MultipartFile multipartFile, FileAssetType fileAssetType, String identifier) {
        if (IMAGE_ASSET_TYPE_IMAGE_LIMIT_SIZE.get(fileAssetType).getValue() < multipartFile.getSize()) {
            throw new InvalidRequestDataException(
                MultipartFile.class,
                Map.of("multipartFile", multipartFile, "fileAssetType", fileAssetType, "identifier", identifier),
                FILE_SIZE_LIMIT_EXCEEDED,
                List.of("multipartFile", "fileAssetType", "identifier")
            );
        }
    }

    /**
     * Throws exception if a given image has unsupported extension.
     *
     * @param multipartFile image file
     * @throws InvalidRequestDataException if validation fails
     */
    void validateImageHasSupportedExtension(MultipartFile multipartFile) {
        final var dot = ".";
        final var extension = dot + StringUtils.substringAfterLast(multipartFile.getOriginalFilename(), dot);
        final var isAllowedExtension = Stream
            .of(ImageExtension.values())
            .anyMatch(imageExtension -> imageExtension.getValue().equals(extension));
        if (!isAllowedExtension) {
            throw new InvalidRequestDataException(MultipartFile.class, "imageExtension", extension, List.of("imageExtension"));
        }
    }

    /**
     * Throws exception if a given user does not have write permission to the file.
     *
     * @param userId        user ID
     * @param fileAssetType asset type
     * @param identifier    asset identifier
     * @throws ForbiddenException if validation fails
     */
    void validateUserHasWritePermissionToFile(long userId, FileAssetType fileAssetType, String identifier) {
        final var id = getLongIdentifier(identifier);

        final var condition =
            switch (fileAssetType) {
                case USER -> userId == id;
                case USER_GROUP -> userGroupMemberRepository.existsByGroupIdAndUserIdAndRole(id, userId, UserGroupRole.OWNER);
                case COURSE -> courseUserDataRepository.existsByCourseIdAndUserIdAndRole(id, userId, CourseRole.OWNER);
                case COURSE_LESSON -> courseUserDataRepository.existsByCourseLessonIdAndUserIdAndRole(
                    id,
                    userId,
                    CourseRole.OWNER
                );
            };

        if (!condition) {
            throw new ForbiddenException(
                MultipartFile.class,
                Map.of("userId", userId, "fileAssetType", fileAssetType, "identifier", identifier),
                List.of("userId", "fileAssetType", "identifier")
            );
        }
    }

    private long getLongIdentifier(String identifier) {
        try {
            return Long.parseLong(identifier);
        } catch (NumberFormatException e) {
            throw new InvalidRequestDataException(String.class, "identifier", identifier);
        }
    }
}
