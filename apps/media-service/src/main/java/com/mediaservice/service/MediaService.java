package com.mediaservice.service;

import com.mediaservice.dao.MediaFileDAO;
import com.mediaservice.dao.MediaInstanceDAO;
import com.mediaservice.dao.UserDAO;
import com.mediaservice.enums.ContentFlag;
import com.mediaservice.model.MediaFile;
import com.mediaservice.model.MediaInstance;
import com.mediaservice.repository.MediaFileRepository;
import com.mediaservice.repository.MediaInstanceRepository;
import com.mediaservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class MediaService {
    @Autowired
    private MediaFileDAO mediaFileDAO;

    @Autowired
    private MediaInstanceDAO mediaInstanceDAO;

    @Autowired
    private UserDAO userDAO;

    /**
     * Used to upload a file.
     * If the file is known, retrieve that file to save a new instance
     * If the file isn't known, save the MediaFile, and use it to save a new instance
     *
     * @param mediaFile, a fresh MediaFile object, no relation to the database
     * @param userId, unique identifier of the user that is uploading the media file
     * @return unique Instance identifier
     */
    public UUID uploadMediaFile(MediaFile mediaFile, UUID userId) {
        if (mediaFileDAO.doesMediaFileExist(mediaFile.getHash())) {
            mediaFile = mediaFileDAO.getMediaFile(mediaFile.getHash());
        }
        else {
            mediaFileDAO.saveMediaFile(mediaFile);
        }
        MediaInstance mediaInstance = new MediaInstance(mediaFile.getId(), userId);
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
     * Retrieves all instances connected to the user to be deleted and sends them to be removed
     *
     * @param userId, unique identifier of the user to be deleted
     */
    public void deleteUser(UUID userId) {
        List<MediaInstance> mediaInstanceList = mediaInstanceDAO.getAllMediaInstancesByUserId(userId);
        mediaInstanceList.forEach(mediaInstance ->
                this.deleteMediaInstance(mediaInstance.getInstanceId()));
        userDAO.deleteUser(userId);
    }
}
