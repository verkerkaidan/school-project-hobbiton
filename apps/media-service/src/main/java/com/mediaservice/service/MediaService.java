package com.mediaservice.service;

import com.mediaservice.dao.MediaFileDAO;
import com.mediaservice.dao.MediaInstanceDAO;
import com.mediaservice.dto.MediaEntryDTO;
import com.mediaservice.enums.ContentFlag;
import com.mediaservice.model.MediaFile;
import com.mediaservice.model.MediaInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class MediaService {
    @Autowired
    private MediaFileDAO mediaFileDAO;

    @Autowired
    private MediaInstanceDAO mediaInstanceDAO;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private CompressionService compressionService;

    /**
     * Used to upload a file.
     * If the file is known, retrieve that file to save a new instance
     * If the file isn't known, save the MediaFile, and use it to save a new instance
     *
     * @param mediaEntryDTO, a mediaEntryDTO object
     * @param ownerId, unique identifier of the user or chat that is uploading the media file
     * @return unique Instance identifier
     */
    public UUID uploadMediaFile(MediaEntryDTO mediaEntryDTO, UUID ownerId) {
        MediaFile mediaFile;
        byte[] hash = encryptionService.createHash(mediaEntryDTO.fileUri);

        if (mediaFileDAO.doesMediaFileExist(hash)) {
            mediaFile = mediaFileDAO.getMediaFile(hash);
        }
        else {
            mediaFile = this.createMediaFile(mediaEntryDTO, hash);
            mediaFileDAO.saveMediaFile(mediaFile);
            this.addFileThumbnail(mediaFile);
        }
        MediaInstance mediaInstance = new MediaInstance(mediaFile.getId(), ownerId);
        mediaInstanceDAO.saveMediaInstance(mediaInstance);

        return mediaInstance.getInstanceId();
    }

    /**
     * Retrieves a mediafile using an instance identifier.
     * Not responsible for checking the existence, that happens a layer down
     *
     * @param instanceId unique identifier of a MediaInstace
     * @return mediaFile, MediaFile object
     */
    public MediaFile getMediaFile(UUID instanceId) {
        MediaInstance instance = mediaInstanceDAO.getMediaInstance(instanceId);
        MediaFile mediaFile = mediaFileDAO.getMediaFile(instance.getFileId());

        return mediaFile;
    }

    /**
     * Retrieves all instances of a given user or chat, and from that retrieves all relevant mediaFiles
     *
     * @param ownerId, unique identifier of the user or chat
     * @return List of MediaFile instances
     */
    public List<MediaFile> getMediaFilesByOwner(UUID ownerId) {
        List<MediaInstance> mediaInstanceList = mediaInstanceDAO.getAllMediaInstancesByOwnerId(ownerId);
        List<UUID> filesIds = mediaInstanceList.stream()
                .map(
                        mediaInstance -> mediaInstance.getFileId()
                ).toList();
        List<MediaFile> mediaFileList = mediaFileDAO.getCollectionOfMediaFiles(filesIds);

        return mediaFileList;
    }

    /**
     * Updating the content flag of a requested file
     *
     * @param fileId, unique identifier of the file to be updated
     * @param contentFlag, ContentFlag enum
     * @return id, unique identifier of the updated MediaFile
     */
    public UUID updateMediaContentFlag(UUID fileId, ContentFlag contentFlag) {
        MediaFile mediaFile = mediaFileDAO.getMediaFile(fileId);
        mediaFile.setContentFlag(contentFlag);
        mediaFileDAO.saveMediaFile(mediaFile);
        return mediaFile.getId();
    }

    /**
     * Deletes a given instance from the database
     * Checks if any instances with the same mediaFile still exist
     * If not, the MediaFile is deleted
     *
     * @param instanceId
     */
    public void deleteMediaInstance(UUID instanceId) {
        MediaInstance mediaInstance = mediaInstanceDAO.getMediaInstance(instanceId);
        mediaInstanceDAO.deleteMediaInstance(mediaInstance);

        List<MediaInstance> mediaInstanceList =
                mediaInstanceDAO.getAllMediaInstancesByMediaFileId(
                        mediaInstance.getFileId()
                );
        if (mediaInstanceList.size() == 0) {
            MediaFile mediaFile = mediaFileDAO.getMediaFile(mediaInstance.getFileId());
            mediaFileDAO.deleteMediaFile(mediaFile);
        }
    }

    /**
     * Retrieves all instances connected to the owner to be deleted and sends them to be removed
     *
     * @param ownerId, unique identifier of the user or chat to be deleted
     */
    public void deleteOwner(UUID ownerId) {
        List<MediaInstance> mediaInstanceList = mediaInstanceDAO.getAllMediaInstancesByOwnerId(ownerId);
        mediaInstanceList.forEach(mediaInstance ->
                this.deleteMediaInstance(mediaInstance.getInstanceId()));
    }

    /**
     * Controls the logic for creating a MediaFile from a MediaEntryDTO
     * Resizes all files to be uniform
     *
     * @param mediaEntryDTO, instance of MediaEntryDTO
     * @param hash, in the form of byte[]
     * @return new MediaFile instance without a connection to database
     */
    private MediaFile createMediaFile(MediaEntryDTO mediaEntryDTO, byte[] hash) {
        String contentType = compressionService.extractContentType(mediaEntryDTO.fileUri);
        byte[] original = compressionService.decodeDataUri(mediaEntryDTO.fileUri);
        byte[] file = compressionService.resizeToUniform(original);

        return new MediaFile(file, hash, ContentFlag.UNASSIGNED, contentType);
    }

    /**
     * After the file was uploaded, a thumbnail is asynchronously created and saved with the MediaFile
     * This way a thumbnail is created for the frontend, without the user having to wait longer
     *
     * @param mediaFile, MediaFile instance
     */
    private void addFileThumbnail(MediaFile mediaFile) {
        Thread thumbThread = new Thread(() -> {
            byte[] thumb = compressionService.resizeToThumb(mediaFile.getOriginal());
            mediaFile.setThumbnail(thumb);
            mediaFileDAO.saveMediaFile(mediaFile);
        });
        thumbThread.start();
    }
}
