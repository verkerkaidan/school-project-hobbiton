package com.recognitionservice.controller;

import com.recognitionservice.DTO.ContentFlagUpdateDTO;
import com.recognitionservice.enums.ContentFlag;
import com.recognitionservice.services.RecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin(origins = {"*"})
@RequestMapping("/recognition")
public class RecognitionController {
    private final RecognitionService recognitionService;

    public RecognitionController(RecognitionService recognitionService) {
        this.recognitionService = recognitionService;
    }

    /**
     * Generates a content risk assessment for each of the given files
     *
     * @param DTOToBeUpdatedList, list of objects with fileId (UUID) and file (byte[]) specified
     * @return list of objects with fileId (UUID) and contentFlag (ContentFlag) specified
     */
    @PostMapping
    public ResponseEntity<List<ContentFlagUpdateDTO>> generateContentFlags(
            @RequestBody List<ContentFlagUpdateDTO> DTOToBeUpdatedList
    ) {
        List<CompletableFuture<ContentFlagUpdateDTO>> futures = DTOToBeUpdatedList.stream()
                .map(dto -> CompletableFuture.supplyAsync(() -> {
                    byte[] file = Base64.getDecoder().decode(dto.file);

                    ContentFlag contentFlag = recognitionService.flagFile(file);
                    System.out.println("Assigned content flag: " + contentFlag.toString() + " to file: " + dto.hash);
                    return new ContentFlagUpdateDTO(dto.hash, contentFlag, "AI driven change");
                }))
                .toList();

        List<ContentFlagUpdateDTO> updatedDTOList = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        return ResponseEntity.ok(updatedDTOList);
    }
}
