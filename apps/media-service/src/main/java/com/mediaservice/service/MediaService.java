package com.mediaservice.service;

import com.mediaservice.model.MediaFile;
import com.mediaservice.model.MediaInstance;
import com.mediaservice.repository.MediaFileRepository;
import com.mediaservice.repository.MediaInstanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MediaService {
    @Autowired
    private MediaFileRepository mediaFileRepository;

    @Autowired
    private MediaInstanceRepository mediaInstanceRepository;

    /**
     * Get a specific media file using the identifier
     *
     * @param id, The identifier of the file
     * @return MediaFile
     * @throws RuntimeException -> Database entry not found
     */
    public MediaFile getMediaFile(UUID id) {
        return mediaFileRepository.getMediaFileByFileId(id)
                .orElseThrow(() -> new RuntimeException("Media file not found"));
    }
    /**
     * Get a specific media file using the hash, so a new file can be linked to the correct identifier
     *
     * @param hash, The hash of the file
     * @return MediaFile
     * @throws RuntimeException -> Database entry not found
     */
    public MediaFile getMediaFile(byte[] hash) {
        return mediaFileRepository.getMediaFileByHash(hash)
                .orElseThrow(() -> new RuntimeException("Media file not found"));
    }

    /**
     * Checks if a given file hash is already a database entry
     *
     * @param hash, hash of a new file
     * @return true if hash is a database entry, false if it doesn't exist
     */
    public boolean doesMediaFileExist(byte[] hash) {
        return mediaFileRepository.existsMediaFileByHash(hash);
    }

    /**
     * Used to upload a mediaFile to the database and to update the mediaInstance accordingly
     * Not responsible for checks for existing files. That happens in the function before
     *
     * @param mediaFile, MediaFile instance created in function before
     * @param userId, identifier of the user, for better MediaInstance relations in the database
     */
    public void uploadMediaFile(MediaFile mediaFile, UUID userId) {
        mediaFileRepository.save(mediaFile);
        MediaInstance mediaInstance = new MediaInstance(mediaFile.getId(), userId);
        mediaInstanceRepository.save(mediaInstance);
    }
}
