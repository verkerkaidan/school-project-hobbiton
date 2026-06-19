package com.recognitionservice.controller;

import com.recognitionservice.DTO.ContentFlagUpdateDTO;
import com.recognitionservice.enums.ContentFlag;
import com.recognitionservice.services.RecognitionService;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/recognition")
public class RecognitionController {

    private static final Logger logger = LoggerFactory.getLogger(RecognitionController.class);
    private final RecognitionService recognitionService;

    public RecognitionController(RecognitionService recognitionService) {
        this.recognitionService = recognitionService;
    }

    /**
     * Generates a content risk assessment for each of the given files
     *
     * @param DTOToBeUpdatedList, list of objects with fileId (UUID) and file
     *                            (byte[]) specified
     * @return list of objects with fileId (UUID) and contentFlag (ContentFlag)
     *         specified
     */
    @PostMapping
    public ResponseEntity<List<ContentFlagUpdateDTO>> generateContentFlags(
        @RequestBody List<ContentFlagUpdateDTO> DTOToBeUpdatedList
    ) {
        List<CompletableFuture<ContentFlagUpdateDTO>> futures =
            DTOToBeUpdatedList.stream()
                .map(dto ->
                    CompletableFuture.supplyAsync(() -> {
                        byte[] file = Base64.getDecoder().decode(dto.file);

                        ContentFlag contentFlag = recognitionService.flagFile(
                            file
                        );
                        logger.info("Assigned content flag {} to file {}", contentFlag.toString(), dto.hash);

                        return new ContentFlagUpdateDTO(
                            dto.hash,
                            contentFlag,
                            "AI driven change"
                        );
                    })
                )
                .toList();

        List<ContentFlagUpdateDTO> updatedDTOList = futures
            .stream()
            .map(CompletableFuture::join)
            .toList();

        return ResponseEntity.ok(updatedDTOList);
    }
}
