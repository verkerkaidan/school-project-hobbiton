package com.mediaservice.dao;


import com.mediaservice.model.MediaFile;
import com.mediaservice.repository.MediaFileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MediaFileDAO {
    @Autowired
    private MediaFileRepository mediaFileRepository;

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
     * Saves MediaFile to database
     *
     * @param mediaFile, as a MediaFile object
     */
    public void saveMediaFile(MediaFile mediaFile) {
        mediaFileRepository.save(mediaFile);
    }

    /**
     * Deletes MediaFile from database
     *
     * @param mediaFile, as a MediaFile object
     */
    public void deleteMediaFile(MediaFile mediaFile) {
        mediaFileRepository.delete(mediaFile);
    }

    /**
     * Retrieves multiple media files
     *
     * @param fileIds, List of file unique identifiers
     * @return List of mediaFile instances
     */
    public List<MediaFile> getCollectionOfMediaFiles(List<UUID> fileIds) {
        return mediaFileRepository.findByFileIdIn(fileIds);
    }
}
