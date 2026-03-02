package com.mediaservice.repository;

import com.mediaservice.model.MediaFile;
import com.mediaservice.model.MediaInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MediaInstanceRepository extends JpaRepository<MediaInstance, UUID> {
    List<MediaInstance> getAllByMediaFile(MediaFile mediaFile);

    List<MediaInstance> getAllByUserId(UUID userId);

    List<MediaInstance> getAllByInstanceId(UUID instanceID);
}
