package com.basatatech.loggingmonitor.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.basatatech.loggingmonitor.util.LogsFileFilterName;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggerService {
    private static final String basedir = "/home/abdelhafeezahmed/logs/";
    private String filePath;
    private String topic = "/topic/messages";
    private SimpMessagingTemplate simpMessagingTemplate;

    public static Map<String, LoggerService> USER_MAP = new HashMap<>();

    private boolean watching = false;

    public LoggerService() {
    }

    public void init(String name, SimpMessagingTemplate simpMessagingTemplate, String userId) {
        log.info("LoggerService init()");
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.filePath = new StringBuilder().append(basedir).append(name).append(".log").toString();
        this.topic = new StringBuilder().append(this.topic).append("/").append(userId).append("/").append(name)
                .toString();
        this.watching = true;
        System.out.println("TOPIC: " + this.topic);
    }

    public void tailFile() {
        log.info("LoggerService tailFile()");
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while (watching) {
                while ((line = reader.readLine()) != null) {
                    log.info("Received file content: " + line);
                    simpMessagingTemplate.convertAndSend(this.topic, line);
                }
                line = null;
            }
            log.info("Exit While *************************");

        } catch (IOException e) {
            log.error("Error reading the file: " + e.getMessage());
        }
    }

    public void stopWatching() {
        this.watching = false;
        log.info("-------------- Stop Watching -----------------------");
    }

    public static LoggerService mapUserSession(String userId) {
        if (USER_MAP.keySet().contains(userId)) {
            return USER_MAP.get(userId);
        }
        LoggerService newSessionLoggerService = new LoggerService();
        USER_MAP.put(userId, newSessionLoggerService);
        return newSessionLoggerService;
    }

    public static void removeUserSession(String userId) {
        log.info("remove session for {}", userId);
        USER_MAP.remove(userId);
    }

    public static List<String> loadLogsNames() {
        List<String> logs = new ArrayList<>();
        File path = new File(basedir);
        if (!path.exists() || !path.isDirectory())
            return null;
        File[] logFiles = path.listFiles(new LogsFileFilterName());
        String fileName;
        for (File file : logFiles) {
            fileName = file.getName();
            int lastDotIndex = fileName.lastIndexOf(".");
            logs.add(fileName.substring(0, lastDotIndex));
        }
        return logs;
    }

}
