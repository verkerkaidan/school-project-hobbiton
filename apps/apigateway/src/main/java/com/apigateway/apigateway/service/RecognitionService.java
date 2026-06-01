package com.apigateway.apigateway.service;

import com.apigateway.apigateway.modelDtos.media.ContentFlagUpdateDTO;
import com.apigateway.apigateway.clients.MediaClient;
import com.apigateway.apigateway.clients.RecognitionClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class RecognitionService {
    private final MediaClient mediaClient;
    private final RecognitionClient recognitionClient;

    public RecognitionService(MediaClient mediaClient, RecognitionClient recognitionClient) {
        this.mediaClient = mediaClient;
        this.recognitionClient = recognitionClient;
    }

    /**
     * Responsible for the recognition assigning pipeline
     * Calling media to get unassigned -> call recognition to assign -> call media to save
     * Waits for 1 day after request was completed to call again
     */
    @Scheduled(fixedDelay = 1000 * 60 * 60 * 24)
    public void automatedRecognitionPipeline() {
        try {
            List<ContentFlagUpdateDTO> withUnassignedFlag = mediaClient.getMediaFilesWithUnassignedFlag();
            if (withUnassignedFlag.isEmpty()) {
                System.out.println("Scheduled job didn't receive urgent files");
                return;
            }
            assignContentFlags(withUnassignedFlag);
        } catch (Exception e) {
            System.err.println("Scheduled job failed: " + e.getMessage());
        }
    }

    /**
     * Starting the recognition pipeline by hand at file upload
     * @param files MultipartFile[]
     */
    public void startRecognitionPipeline(List<MultipartFile> files) {
        List<ContentFlagUpdateDTO> dtos = new ArrayList<>();
        files.forEach((MultipartFile file) -> {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");

                byte[] bytes = file.getBytes();
                byte[] hash = digest.digest(bytes);
                String base64Hash = Base64.getEncoder().encodeToString(hash);
                String base64File = Base64.getEncoder().encodeToString(bytes);

                dtos.add(new ContentFlagUpdateDTO(base64File, base64Hash));
            } catch (Exception e) {
                System.out.println("Error occurred while hashing a file: " + e.getMessage());
            }
        });
        assignContentFlags(dtos);
    }

    /**
     * Handles the request to the recognition service, and the update to the media service
     * @param files {file: base64String, hash: base64String}
     */
    private void assignContentFlags(List<ContentFlagUpdateDTO> files) {
        CompletableFuture.runAsync(() -> {
            List<ContentFlagUpdateDTO> results = recognitionClient.assignContentFlags(files);
            results.forEach((ContentFlagUpdateDTO dto) -> {
                System.out.println("[RECOGNITION SERVICE assignContentFlags] note: " + dto.note + " -- hash: " + dto.hash);
            });
            mediaClient.assignContentFlags(results);
        }).exceptionally(ex -> {
            System.err.println("assignContentFlags failed: " + ex);
            return null;
        });
    }
}
