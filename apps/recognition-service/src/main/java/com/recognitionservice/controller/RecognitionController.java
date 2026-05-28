package com.recognitionservice.controller;

import com.recognitionservice.DTO.ContentFlagUpdateDTO;
import com.recognitionservice.enums.ContentFlag;
import com.recognitionservice.services.RecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@CrossOrigin(origins = {"*"})
@RequestMapping("/recognition")
public class RecognitionController {
    @Autowired
    private RecognitionService recognitionService;

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
                    ContentFlag contentFlag = recognitionService.flagFile(dto.file);
                    System.out.println("Assigned content flag: " + contentFlag.toString() + " to file: " + dto.id);
                    return new ContentFlagUpdateDTO(dto.id, contentFlag, "AI driven change");
                }))
                .toList();

        List<ContentFlagUpdateDTO> updatedDTOList = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        return ResponseEntity.ok(updatedDTOList);
    }
}
