package com.mediaservice.controller;

import com.mediaservice.dto.ContentFlagUpdateDTO;
import com.mediaservice.dto.MediaEntryDTO;
import com.mediaservice.dto.MediaFileDTO;
import com.mediaservice.model.MediaFile;
import com.mediaservice.service.MediaService;
import com.mediaservice.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/media")
public class MediaController {

    @Autowired
    private MediaService mediaService;
    @Autowired
    private ReportService reportService;

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
    public ResponseEntity<List<MediaFileDTO>> retrieveMediaByOwner(@PathVariable UUID ownerId) {
        List<MediaFile> mediaFileList = mediaService.getMediaFilesByOwner(ownerId);
        List<MediaFileDTO> dtoList = mediaFileList.stream().map(
            (mediaFile -> new MediaFileDTO(
                        mediaFile.getOriginal(),
                        mediaFile.getThumbnail(),
                        mediaFile.getContentType(),
                        mediaFile.getContentFlag()
            ))).toList();
        return ResponseEntity.ok(dtoList);
    }

    /**
     * Removes the given instance
     * If no instances of the corresponding mediaFile remain, the MediaFile should also be deleted
     * DELETE /api/media/{instanceId}
     *
     * @param instanceId is unique identifier of a mediaInstance
     * @return UUID, uuid of deleted instance, confirming deletion
     */
    @DeleteMapping("/file/{instanceId}")
    public ResponseEntity<UUID> deleteInstance(@PathVariable UUID instanceId) {
        mediaService.deleteMediaInstance(instanceId);
        return ResponseEntity.ok(instanceId);
    }

    /**
     * Deletes all instances of the given owner, and deletes mediaFiles if relevant
     *
     * @param ownerId, unique identifier of the owner being deleted
     * @return UUID, uuid of deleted owner, confirming deletion
     */
    @DeleteMapping("/owner/{ownerId}")
    public ResponseEntity<UUID> deleteOwner(@PathVariable UUID ownerId) {
        mediaService.deleteOwner(ownerId);
        return ResponseEntity.ok(ownerId);
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
    public ResponseEntity<List<UUID>> updateContentFlag(
            @RequestBody List<ContentFlagUpdateDTO> files
    ) {
        List<UUID> updatedMediaFiles = new ArrayList<>();
        for (int i=0; i<files.size();i++) {
            ContentFlagUpdateDTO file = files.get(i);
            mediaService.updateMediaContentFlag(file.fileId, file.contentFlag);
            updatedMediaFiles.add(file.fileId);
        }

        reportService.updateReports(files);

        return ResponseEntity.ok(updatedMediaFiles);
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

    /**
     * When a user reports an image, an admin needs to be able to get the full MediaFile.
     * UUID of the file (not instance) is needed to change the contentFlag using the @PutMapping
     *
     * @param instanceId, UUID
     * @return MediaFile Object
     */
    @GetMapping("/admin/{instanceId}")
    public ResponseEntity<MediaFile> getCompleteMediaFile(@PathVariable UUID instanceId) {
        return ResponseEntity.ok(mediaService.getMediaFile(instanceId));
    }

    /**
     * When a user reports a file, the instance is sent, and used to note the file that was reported
     *
     * @param userId, identifier of the user that reported
     * @param instanceId, identifier of the instance that was reported
     * @return reportId, UUID identifier of the report
     */
    @PostMapping("/report/{userId}/{instanceId}")
    public ResponseEntity<UUID> createNewFileReport(@PathVariable UUID userId, @PathVariable UUID instanceId) {
        UUID reportId = reportService.createNewReport(userId, instanceId);
        return ResponseEntity.ok(reportId);
    }
}
