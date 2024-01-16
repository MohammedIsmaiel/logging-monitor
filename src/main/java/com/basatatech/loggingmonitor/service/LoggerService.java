package com.basatatech.loggingmonitor.service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class LoggerService {

    private final String filePath;
    private SimpMessagingTemplate simpMessagingTemplate;

    public void tailFile() {
        // Implement your file-tailing logic here
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while (true) {
                while ((line = reader.readLine()) != null) {
                    // Your business logic to process the file content goes here
                    log.info("Received file content: " + line);
                    simpMessagingTemplate.convertAndSend("/topic/messages", line);
                }
            }

        } catch (IOException e) {
            log.error("Error reading the file: " + e.getMessage());
        }
    }

}
