package com.mediaservice.controller;

import com.mediaservice.model.MediaFile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/media")
public class MediaController {
    /**
     * Upload a mediafile to database
     * PUT /api/media
     * Request body: MediaFile model
     *
     * If the mediafile's hash is present in database, it will not be uploaded again
     * Regardless of it being saved now or before, the unique identifier will be retrieved
     *
     * @param mediaFile model
     * @return id of mediafile in database
     */
    @PutMapping
    public ResponseEntity<String> uploadMedia(@RequestBody MediaFile mediaFile) {
        return ResponseEntity.ok("FileIdentifier");
    }

    /**
     * Retrieves a single file using given unique identifier
     * GET /api/media/{mediaId}
     *
     * @param mediaId is the unique identifier of a media file
     * @return mediaFile in the form of a base64 string (Will be encrypted)
     */
    @GetMapping("/{mediaId}")
    public ResponseEntity<String> retrieveMedia(@PathVariable UUID mediaId) {
        return ResponseEntity.ok("Base64");
    }

    /**
     * Removes one instance of the given userId from the mediaFile instances
     * If no instances of the mediaFile remain, the MediaFile itself should be deleted
     * DELETE /api/media/{mediaId}/{userId} //TODO Should this be changed to @RequestBody params instead of @PathVariables?
     *
     * @param mediaId is unique identifier of a media file
     */
    @DeleteMapping("/{mediaId}/{userId}")
    public ResponseEntity.BodyBuilder deleteUserFile(@PathVariable UUID mediaId, @PathVariable UUID userId) {
        return ResponseEntity.ok();
    }
}
