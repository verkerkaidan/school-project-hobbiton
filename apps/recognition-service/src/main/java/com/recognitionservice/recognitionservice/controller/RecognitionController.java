package com.recognitionservice.recognitionservice.controller;

import com.recognitionservice.recognitionservice.DTO.ContentFlagUpdateDTO;
import com.recognitionservice.recognitionservice.enums.ContentFlag;
import com.recognitionservice.recognitionservice.services.RecognitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/recognition")
public class RecognitionController {
    @Autowired
    private RecognitionService recognitionService;

    @GetMapping
    public ResponseEntity<List<ContentFlagUpdateDTO>> generateContentFlags(
            @RequestBody List<ContentFlagUpdateDTO> DTOToBeUpdatedList
    ) {
        List<CompletableFuture<ContentFlagUpdateDTO>> futures = DTOToBeUpdatedList.stream()
                .map(dto -> CompletableFuture.supplyAsync(() -> {
                    ContentFlag contentFlag = recognitionService.flagFile(dto.file);
                    return new ContentFlagUpdateDTO(dto.fileId, contentFlag);
                }))
                .toList();

        List<ContentFlagUpdateDTO> updatedDTOList = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        return ResponseEntity.ok(updatedDTOList);
    }
}
