package com.mediaservice.controller;

import com.mediaservice.enums.ContentFlag;
import com.mediaservice.model.MediaFile;
import com.mediaservice.service.CompressionService;
import com.mediaservice.service.EncryptionService;
import com.mediaservice.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private CompressionService compressionService;

    /**
     * Upload a mediafile to database
     * POST /api/media/{userId}
     * Request body: base64 string of media file
     *
     * If the mediafile's hash is present in database, it will not be uploaded again
     * Regardless of it being saved now or before, the unique identifier will be retrieved
     *
     * @param fileData string in the form of base64 (encrypted)
     * @param userId Identifier of the user that is uploading the image
     * @return id of mediafile in database
     */
    @PostMapping("/{userId}")
    public ResponseEntity<UUID> uploadMedia(@RequestBody String fileData, @PathVariable UUID userId) {
        MediaFile mediaFile;
        byte[] hash = encryptionService.createHash(fileData);
        if (mediaService.doesMediaFileExist(hash)) {
            mediaFile = mediaService.getMediaFile(hash);
        } else {
            mediaFile = new MediaFile(hash, ContentFlag.NONE, fileData);
            mediaService.uploadMediaFile(mediaFile, userId);
        }
        return ResponseEntity.ok(mediaFile.getId());
    }

    /**
     * Retrieves a single file, using given unique identifier
     * GET /api/media/{mediaId}
     *
     * @param mediaId is the unique identifier of a media file
     * @return MediaFile instance (Will be encrypted)
     */
    @GetMapping("/{mediaId}")
    public ResponseEntity<MediaFile> retrieveMedia(@PathVariable UUID mediaId) {
        MediaFile mediaFile = mediaService.getMediaFile(mediaId);
        return ResponseEntity.ok(mediaFile);

    }

    /**
     * Removes one instance of the given userId from the mediaFile instances
     * If no instances of the mediaFile remain, the MediaFile itself should be deleted
     * DELETE /api/media/{mediaId}/{userId} //TODO Should this be changed to @RequestBody params instead of @PathVariables?
     *
     * @param mediaId is unique identifier of a media file
     */
    @DeleteMapping("/{mediaId}/{userId}")
    public ResponseEntity.BodyBuilder deleteUserFile(@PathVariable UUID mediaId, @PathVariable UUID userId) {
        return ResponseEntity.ok();
    }
}
