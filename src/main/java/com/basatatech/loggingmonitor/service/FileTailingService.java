package com.basatatech.loggingmonitor.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.*;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class FileTailingService {

    private final String filePath;
    private SimpMessagingTemplate simpMessagingTemplate;
    private boolean watching = false;

    public FileTailingService(String filePath, SimpMessagingTemplate simpMessagingTemplate) {
        this.filePath = filePath;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void tailFile() {
        if (watching) {
            log.info("Already watching the file: " + filePath);
            return;
        }

        Path path = Paths.get(filePath);

        try {
            // Create a WatchService
            WatchService watchService = FileSystems.getDefault().newWatchService();

            // Register the directory with the WatchService for ENTRY_MODIFY events
            path.getParent().register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            // Start watching
            watching = true;

            while (watching) {
                // Wait for a key to be signaled
                WatchKey key = watchService.take();

                for (WatchEvent<?> event : key.pollEvents()) {
                    if (event.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        Path modifiedFile = (Path) event.context();
                        if (modifiedFile.equals(path.getFileName())) {
                            // Read and process the newly added content
                            processNewContent();
                        }
                    }
                }

                // Reset the key, so that it can be watched again
                boolean valid = key.reset();
                if (!valid) {
                    log.error("WatchKey is no longer valid, stopping file-tailing.");
                    break;
                }
            }

        } catch (IOException | InterruptedException e) {
            log.error("Error tailing file: " + e.getMessage());
        }
    }

    private void processNewContent() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Your business logic to process the file content goes here
                log.info("Received file content: " + line);
                simpMessagingTemplate.convertAndSend("/topic/messages", line);
            }
        } catch (IOException e) {
            log.error("Error reading the file: " + e.getMessage());
        }
    }

    public void stopTailing() {
        watching = false;
    }
}
