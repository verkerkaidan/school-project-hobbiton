package com.mediaservice.controller;

import com.mediaservice.dto.MediaFileDTO;
import com.mediaservice.enums.ContentFlag;
import com.mediaservice.model.MediaFile;
import com.mediaservice.service.EncryptionService;
import com.mediaservice.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;

    @Autowired
    private EncryptionService encryptionService;

    /**
     * Upload a media file to database
     * POST /api/media/{userId}
     * Request body: MediaFileDTO with b64 string of file_data and possible contentFlag
     *
     * If the media file's hash is present in database, it will not be uploaded again
     * Regardless of it being saved now or before, the unique identifier will be retrieved
     *
     * @param mediaFileDTO string in the form of base64 dataURI (encrypted), and possible contentFlag
     * @param userId Identifier of the user that is uploading the image
     * @return id of media file in database
     */
    @PostMapping("/{userId}")
    public ResponseEntity<UUID> uploadMedia(@RequestBody MediaFileDTO mediaFileDTO, @PathVariable UUID userId) {
        byte[] hash = encryptionService.createHash(mediaFileDTO.fileData);
        ContentFlag contentFlag =
                Objects.requireNonNullElse(mediaFileDTO.contentFlag, ContentFlag.NONE);
        MediaFile mediaFile = new MediaFile(hash, contentFlag, mediaFileDTO.fileData);

        UUID instanceId = mediaService.uploadMediaFile(mediaFile, userId);

        return ResponseEntity.ok(instanceId);
    }

    /**
     * Retrieves a single file, using given unique identifier of instance
     * GET /api/media/{instanceId}
     *
     * @param instanceId is the unique identifier of a media instance
     * @return MediaFileDTO instance (Will be encrypted)
     */
    @GetMapping("/{instanceId}")
    public ResponseEntity<MediaFileDTO> retrieveMedia(@PathVariable UUID instanceId) {
        MediaFile mediaFile = mediaService.getMediaFile(instanceId);
        MediaFileDTO mediaDto = new MediaFileDTO(mediaFile.getFileData(), mediaFile.getContentFlag());
        return ResponseEntity.ok(mediaDto);
    }

    /**
     * Removes the given instance
     * If no instances of the mediaFile remain, the MediaFile should also be deleted
     * DELETE /api/media/{instanceId}
     *
     * @param instanceId is unique identifier of a mediaInstance
     */
    @DeleteMapping("/{instanceId}")
    public ResponseEntity<String> deleteInstance(@PathVariable UUID instanceId) {
        return ResponseEntity.ok("Successful deletion");
    }
}
