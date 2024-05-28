package com.basatatech.loggingmonitor.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.basatatech.loggingmonitor.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogService {
    private static final String LOGS_DIR = "/home/srvadmin/APP/logs/";
    private static final Map<String, LogService> USER_MAP = new HashMap<>();
    private static final Map<String, String> LOG_PATH_MAP = new HashMap<>();
    private static final Map<String, String> ARCHIVE_PATH_MAP = new HashMap<>();

    private String filePath;
    private String topic = "/topic/messages";
    private SimpMessagingTemplate simpMessagingTemplate;
    private boolean watching = false;

    public LogService() {
        // Default constructor for LogService
    }

    public static String readLog(String userId, String name, boolean archive,
            SimpMessagingTemplate simpMessagingTemplate) {
        LogService loggerService = mapUserSession(userId);
        String logPath = archive ? getArchivePath(name) : getLogPath(name);
        if (logPath != null) {
            log.info("--------------------{}-------------------", name);
            loggerService.init(logPath, simpMessagingTemplate, userId);
            return loggerService.readFile(archive);
        } else {
            return "Log path not found";
        }
    }

    public static String stopLog(String userId, String name) {
        log.info("--------------------{}-------------------", name);
        LogService loggerService = mapUserSession(userId);
        if (loggerService != null) {
            loggerService.stopWatching();
            removeUserSession(userId);
            return "done";
        } else {
            return "Session not found";
        }
    }

    public void init(String logPath, SimpMessagingTemplate simpMessagingTemplate, String userId) {
        log.info("LoggerService init()");
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.filePath = logPath;
        this.topic = String.format("%s/%s/%s", topic, userId, new File(logPath).getName());
        this.watching = true;
        log.info("TOPIC: {}", this.topic);
    }

    public String readFile(boolean archive) {
        log.info("LoggerService readFile() for file: {}", filePath);
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            if (archive) {
                while ((line = reader.readLine()) != null) {
                    builder.append(line).append("\n");
                }
                return builder.toString(); // Return the full content for archive
            } else {
                while (watching) {
                    while ((line = reader.readLine()) != null) {
                        log.info("Received file content: {}", line);
                        builder.append(line).append("\n");
                    }
                    if (builder.length() > 0) {
                        String message = builder.toString();
                        simpMessagingTemplate.convertAndSend(FileUtil.removeLogSuffix(topic), message);
                        builder.setLength(0); // Clear the builder for the next iteration
                    }
                }
                log.info("Exit While *************************");
            }
        } catch (IOException e) {
            log.error("Error reading the file: {}", e.getMessage());
        }
        return null; // For live logs, return null
    }

    public void stopWatching() {
        this.watching = false;
        log.info("-------------- Stop Watching -----------------------");
    }

    public static LogService mapUserSession(String userId) {
        return USER_MAP.computeIfAbsent(userId, k -> new LogService());
    }

    public static void removeUserSession(String userId) {
        log.info("remove session for {}", userId);
        USER_MAP.remove(userId);
    }

    public static void storeLogPath(String logName, String logPath) {
        LOG_PATH_MAP.put(logName, logPath);
    }

    public static String getLogPath(String logName) {
        return LOG_PATH_MAP.get(logName);
    }

    public static void storeArchivePaths(String archiveName, String archivePath) {
        ARCHIVE_PATH_MAP.put(archiveName, archivePath);
    }

    public static String getArchivePath(String archiveName) {
        return ARCHIVE_PATH_MAP.get(archiveName);
    }

    public static List<String> loadLogsNames() {
        return FileUtil.loadLogsNames(LOGS_DIR);
    }

    public static List<String> loadArchiveNames(String logName) {
        return FileUtil.loadArchiveNames(LOG_PATH_MAP.get(logName));
    }
}