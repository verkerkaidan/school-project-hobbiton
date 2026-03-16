package com.mediaservice.controller;

import com.mediaservice.dto.ContentFlagUpdateDTO;
import com.mediaservice.dto.MediaEntryDTO;
import com.mediaservice.dto.MediaFileDTO;
import com.mediaservice.enums.ContentFlag;
import com.mediaservice.model.MediaFile;
import com.mediaservice.service.CompressionService;
import com.mediaservice.service.EncryptionService;
import com.mediaservice.service.MediaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;

    /**
     * Upload a media file to database
     * If the media file's hash is present in database, it will not be uploaded again
     * Regardless of it being saved now or before, the unique identifier will be retrieved
     * POST /api/media/{ownerId}
     * Request body: MediaEntryDTO with string of fileUri
     *
     * @param mediaEntryDTO string in the form of base64 dataURI (encrypted)
     * @param ownerId Identifier of the owner that is uploading the image
     * @return id of media file in database
     */
    @PostMapping("/{ownerId}")
    public ResponseEntity<UUID> uploadMedia(@RequestBody MediaEntryDTO mediaEntryDTO, @PathVariable UUID ownerId) {
        UUID instanceId = mediaService.uploadMediaFile(mediaEntryDTO, ownerId);
        return ResponseEntity.ok(instanceId);
    }

    /**
     * Retrieves a single file, using given unique identifier of instance
     * GET /api/media/file/{instanceId}
     *
     * @param instanceId is the unique identifier of a media instance
     * @return MediaFileDTO instance with byte[] content, String contentType and ContentFlag contentFlag
     */
    @GetMapping("/file/{instanceId}")
    public ResponseEntity<MediaFileDTO> retrieveMedia(@PathVariable UUID instanceId) {
        MediaFile mediaFile = mediaService.getMediaFile(instanceId);
        MediaFileDTO mediaDto = new MediaFileDTO(
                mediaFile.getOriginal(),
                mediaFile.getThumbnail(),
                mediaFile.getContentType(),
                mediaFile.getContentFlag()
        );
        return ResponseEntity.ok(mediaDto);
    }

    /**
     * Retrieves all mediaFiles that are connected to given owner unique identifier
     * GET /api/media/owner/{ownerId}
     *
     * @param ownerId, unique identifier of the user or chat whose files should be retrieved
     * @return List of MediaFile instances
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<MediaFile>> retrieveMediaByOwner(@PathVariable UUID ownerId) {
        List<MediaFile> mediaFileList = mediaService.getMediaFilesByOwner(ownerId);
        return ResponseEntity.ok(mediaFileList);
    }

    /**
     * Removes the given instance
     * If no instances of the corresponding mediaFile remain, the MediaFile should also be deleted
     * DELETE /api/media/{instanceId}
     *
     * @param instanceId is unique identifier of a mediaInstance
     * @return String, confirming deletion
     */
    @DeleteMapping("/file/{instanceId}")
    public ResponseEntity<String> deleteInstance(@PathVariable UUID instanceId) {
        mediaService.deleteMediaInstance(instanceId);
        return ResponseEntity.ok("Successful deletion");
    }

    /**
     * Deletes all instances of the given owner, and deletes mediaFiles if relevant
     *
     * @param ownerId, unique identifier of the owner being deleted
     * @return String, confirming deletion
     */
    @DeleteMapping("/owner/{ownerId}")
    public ResponseEntity<String> deleteOwner(@PathVariable UUID ownerId) {
        mediaService.deleteOwner(ownerId);
        return ResponseEntity.ok("Successful deletion");
    }

    /**
     * Update the content flag for all given files
     * Should only be called by api-gateway, after AI-response
     * No action in frontend should be the direct cause of this endpoint being called
     *
     * @param files, List of ContentFlagUpdateDTO instances with ContentFlag enum and file identifier
     * @return id, unique identifier of the changed MediaFile object
     */
    @PutMapping
    public ResponseEntity<String> updateContentFlag(
            @RequestBody List<ContentFlagUpdateDTO> files
    ) {
        for (int i=0; i<files.size();i++) {
            ContentFlagUpdateDTO file = files.get(i);
            mediaService.updateMediaContentFlag(file.fileId, file.contentFlag);
        }

        return ResponseEntity.ok("Succesful update");
    }

    /**
     * Retrieves all MediaFiles which don't yet have a content flag assigned
     *
     * @return ContentFlagUpdateDTO, with file identifier, contentFlag and file byte[]
     */
    @GetMapping
    public ResponseEntity<List<ContentFlagUpdateDTO>> getUnassignedMediaFiles() {
        List<MediaFile> allUnassigned = mediaService.getAllUnassigned();
        List<ContentFlagUpdateDTO> updateDTOS = allUnassigned.stream()
                .map(
                        mediaFile -> new ContentFlagUpdateDTO(
                                mediaFile.getFileId(),
                                mediaFile.getOriginal()
                        )
                ).toList();

        return ResponseEntity.ok(updateDTOS);
    }
}
