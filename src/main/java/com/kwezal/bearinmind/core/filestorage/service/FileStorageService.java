package com.kwezal.bearinmind.core.filestorage.service;

import static java.util.Objects.isNull;

import com.kwezal.bearinmind.core.auth.service.LoggedInUserService;
import com.kwezal.bearinmind.core.filestorage.ennumeration.FileAssetType;
import com.kwezal.bearinmind.exception.InvalidRequestDataException;
import com.kwezal.bearinmind.filestorage.api.FileStorageClientApi;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@Service
public class FileStorageService {

    private final FileStorageClientApi fileStorageClientApi;
    private final FileValidationService fileValidationService;
    private final LoggedInUserService loggedInUserService;

    public String uploadImage(final MultipartFile multipartFile, final FileAssetType fileAssetType, final String identifier) {
        fileValidationService.validateImageHasSupportedExtension(multipartFile);
        fileValidationService.validateImageDoesNotExceedSizeLimit(multipartFile, fileAssetType, identifier);
        fileValidationService.validateUserHasWritePermissionToFile(
            loggedInUserService.getLoggedInUserId(),
            fileAssetType,
            identifier
        );

        final var file = getFile(multipartFile);
        final var fileUrl = fileStorageClientApi.upload(file, fileAssetType.name(), identifier);
        file.deleteOnExit();
        return fileUrl;
    }

    private File getFile(final MultipartFile multipartFile) {
        final var filename = getFilename(multipartFile);
        final var file = new File(filename);
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(multipartFile.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    private String getFilename(final MultipartFile multipartFile) {
        if (isNull(multipartFile.getOriginalFilename())) {
            throw new InvalidRequestDataException(
                MultipartFile.class,
                Map.of("filename", Objects.toString(multipartFile.getOriginalFilename()))
            );
        }
        return (
            Long.toString(System.currentTimeMillis(), Character.MAX_RADIX) +
            StringUtils.right(multipartFile.getOriginalFilename(), 4)
        );
    }

    public void delete(final String url) {
        fileStorageClientApi.delete(url);
    }
}
