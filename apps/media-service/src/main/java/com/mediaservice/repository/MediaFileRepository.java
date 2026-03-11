package com.mediaservice.repository;

import com.mediaservice.model.MediaFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {
    // Retrieve the media file that matches the given file identifier
    Optional<MediaFile> getMediaFileByFileId(UUID id);

    // Retrieve the media file that matches the given hash
    Optional<MediaFile> getMediaFileByHash(byte[] hash);

    // Check if a given hash is present in the table
    Boolean existsMediaFileByHash(byte[] hash);

    // Retrieve all files from a collection of file identifiers
    List<MediaFile> findByFileIdIn(Collection<UUID> fileIds);
}
