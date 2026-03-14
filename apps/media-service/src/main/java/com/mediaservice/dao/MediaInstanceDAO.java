package com.mediaservice.dao;

import com.mediaservice.model.MediaInstance;
import com.mediaservice.repository.MediaInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MediaInstanceDAO {
    @Autowired
    private MediaInstanceRepository mediaInstanceRepository;

    /**
     * Get a specific media instance from unique identifier
     *
     * @param id, unique identifier of MediaInstance
     * @return MediaInstance object
     */
    public MediaInstance getMediaInstance(UUID id) {
        return mediaInstanceRepository.getByInstanceId(id)
                .orElseThrow(() -> new RuntimeException("Media instance not found"));
    }

    /**
     * Retrieves all media instances of a specific owner
     * Used for account deletion purposes
     *
     * @param id, unique identifier of a user or chat
     * @return list of MediaInstances
     */
    public List<MediaInstance> getAllMediaInstancesByOwnerId(UUID id) {
        return mediaInstanceRepository.getAllByOwnerId(id);
    }

    /**
     * Retrieves all media instances of a specific MediaFile
     * Used for content deletion purposes
     *
     * @param id, unique identifier of a MediaFileObject
     * @return list of MediaInstances
     */
    public List<MediaInstance> getAllMediaInstancesByMediaFileId(UUID id) {
        return mediaInstanceRepository.getAllByFileId(id);
    }

    /**
     * Saving a MediaInstance to database
     *
     * @param mediaInstance
     */
    public void saveMediaInstance(MediaInstance mediaInstance) {
        mediaInstanceRepository.save(mediaInstance);
    }

    /**
     * Deletes a MediaInstance from database
     *
     * @param mediaInstance, as MediaInstance object
     */
    public void deleteMediaInstance(MediaInstance mediaInstance) {
        mediaInstanceRepository.delete(mediaInstance);
    }
}
