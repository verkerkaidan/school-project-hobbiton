package com.mediaservice.service;

import com.mediaservice.dao.MediaFileDAO;
import com.mediaservice.dao.MediaInstanceDAO;
import com.mediaservice.model.MediaFile;
import com.mediaservice.model.MediaInstance;
import com.mediaservice.repository.MediaFileRepository;
import com.mediaservice.repository.MediaInstanceRepository;
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
}
