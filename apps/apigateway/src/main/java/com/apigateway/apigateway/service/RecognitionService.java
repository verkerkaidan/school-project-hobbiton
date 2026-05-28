package com.apigateway.apigateway.service;

import com.apigateway.apigateway.modelDtos.media.ContentFlagUpdateDTO;
import com.apigateway.apigateway.clients.MediaClient;
import com.apigateway.apigateway.clients.RecognitionClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

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
     * Waits for 5 minutes after request was completed to call again
     */
    @Scheduled(fixedDelay = 1000 * 60 * 5)
    public void runRecognitionPipeline() {
        try {
            List<ContentFlagUpdateDTO> withUnassignedFlag = mediaClient.getMediaFilesWithUnassignedFlag();
            if (withUnassignedFlag.size() == 0) {
                System.out.println("Scheduled job didn't receive urgent files");
                return;
            }
            assignContentFlags(withUnassignedFlag);
        } catch (Exception e) {
            System.err.println("Scheduled job failed: " + e.getMessage());
        }
    }

    public void assignContentFlags(List<ContentFlagUpdateDTO> files) {
        Thread assignationThread = new Thread(() -> {
            List<ContentFlagUpdateDTO> results = recognitionClient.assignContentFlags(files);
            mediaClient.assignContentFlags(results);
            System.out.println("Updated ContentFlags via mediaService");
        });
        assignationThread.start();
    }
}
