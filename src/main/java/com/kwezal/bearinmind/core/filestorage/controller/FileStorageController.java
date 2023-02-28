package com.kwezal.bearinmind.core.filestorage.controller;

import com.kwezal.bearinmind.core.filestorage.ennumeration.FileAssetType;
import com.kwezal.bearinmind.core.filestorage.service.FileStorageService;
import com.kwezal.bearinmind.core.logging.ControllerLogging;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Validated
@RestController
@RequestMapping("/file")
@ControllerLogging("/file")
@RequiredArgsConstructor
public class FileStorageController {

    private final FileStorageService fileStorageService;

    /**
     * Uploads the image to the file storage pointing to the asset it concerns.
     *
     * @param multipartFile image file
     * @param fileAssetType asset type
     * @param identifier    asset identifier
     * @return URL to the saved file
     */
    @PostMapping("/image")
    public String uploadImage(
        @RequestPart(value = "file") MultipartFile multipartFile,
        @RequestParam FileAssetType fileAssetType,
        @RequestParam String identifier
    ) {
        return fileStorageService.uploadImage(multipartFile, fileAssetType, identifier);
    }

    /**
     * Deletes the file of a given URL from the file storage.
     *
     * @param url file URL
     */
    @DeleteMapping
    public void delete(@RequestParam String url) {
        fileStorageService.delete(url);
    }
}
