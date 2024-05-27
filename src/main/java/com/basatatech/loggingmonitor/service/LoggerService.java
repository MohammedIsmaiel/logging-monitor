package com.basatatech.loggingmonitor.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggerService {
    private static final String LOGS_DIR = "/home/srvadmin/APP/logs/";
    private String filePath;
    private String topic = "/topic/messages";
    private SimpMessagingTemplate simpMessagingTemplate;

    private static final Map<String, LoggerService> USER_MAP = new HashMap<>();
    private static final Map<String, String> LOG_PATH_MAP = new HashMap<>();
    private static final Map<String, List<String>> ARCHIVE_PATH_MAP = new HashMap<>();

    private boolean watching = false;

    public LoggerService() {
        // document why this constructor is empty
    }

    public void init(String logPath, SimpMessagingTemplate simpMessagingTemplate, String userId) {
        log.info("LoggerService init()");
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.filePath = logPath;
        this.topic = this.topic + "/" + userId + "/" + new File(logPath).getName();
        this.watching = true;
        System.out.println("TOPIC: " + this.topic);
        // Iterate over LOG_PATH_MAP entries and log them
        for (Map.Entry<String, String> entry : LOG_PATH_MAP.entrySet()) {
            String logName = entry.getKey();
            String logPath2 = entry.getValue();
            log.info("Log Name: {}, Log Path: {}", logName, logPath2);
        }

        // Iterate over ARCHIVE_PATH_MAP entries and log them
        for (Map.Entry<String, List<String>> entry : ARCHIVE_PATH_MAP.entrySet()) {
            String logName = entry.getKey();
            List<String> archivePaths = entry.getValue();
            log.info("Log Name: {}, Archive Paths: {}", logName, archivePaths);
        }
    }

    public void tailFile() {
        log.info("LoggerService tailFile() for file: {}", filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            StringBuilder builder;
            while (watching) {
                builder = new StringBuilder();
                line = null;
                while ((line = reader.readLine()) != null) {
                    log.info("Received file content: " + line);
                    builder.append(line).append("\n");
                }
                if (builder != null && builder.length() > 0) {
                    String message = Objects.requireNonNull(builder.toString(), "Message is null");
                    String localTopic = Objects.requireNonNull(this.topic, "Topic is null");
                    simpMessagingTemplate.convertAndSend(localTopic, message);
                    log.info("Buffer Sent");
                }
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

    public static void storeLogPath(String logName, String logPath) {
        LOG_PATH_MAP.put(logName, logPath);
    }

    public static String getLogPath(String logName) {
        return LOG_PATH_MAP.get(logName);
    }

    public static void storeArchivePaths(String logName, List<String> archivePaths) {
        ARCHIVE_PATH_MAP.put(logName, archivePaths);
    }

    public static List<String> getArchivePaths(String logName) {
        return ARCHIVE_PATH_MAP.getOrDefault(logName, Collections.emptyList());
    }

    public static List<String> loadLogsNames() {
        List<String> logs = new ArrayList<>();
        File path = new File(LOGS_DIR);
        if (!path.exists() || !path.isDirectory())
            return Collections.emptyList();
        File[] subDirs = path.listFiles(File::isDirectory);
        if (subDirs != null) {
            for (File subDir : subDirs) {
                loadLogsFromSubDir(subDir, logs);
            }
        }
        return logs;
    }

    private static void loadLogsFromSubDir(File dir, List<String> logs) {
        File[] logFiles = dir.listFiles(new LogsFileFilterName());
        if (logFiles != null) {
            for (File file : logFiles) {
                if (file.isDirectory()) {
                    loadLogsFromSubDir(file, logs);
                } else {
                    String fileName = file.getName();
                    int lastDotIndex = fileName.lastIndexOf(".");
                    if (lastDotIndex != -1) {
                        String logName = fileName.substring(0, lastDotIndex);
                        logs.add(logName);
                        storeLogPath(logName, file.getAbsolutePath());
                    }
                }
            }
        }
    }

    private static class LogsFileFilterName implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            File file = new File(dir, name);
            return file.isDirectory() || name.endsWith(".log");
        }
    }

    public static List<String> loadArchiveNames(String logName) {
        List<String> archives = new ArrayList<>();
        String logDirPath = LOG_PATH_MAP.get(logName);
        if (logDirPath != null) {
            File logDir = new File(logDirPath).getParentFile();
            try {
                if (logDir.exists() && logDir.isDirectory()) {
                    loadArchivesFromDir(logDir, archives);
                    storeArchivePaths(logName, archives);
                }
            } catch (Exception e) {
                log.error("Error loading archive names for log: " + logName, e);
            }
        }
        return archives;
    }

    private static void loadArchivesFromDir(File dir, List<String> archives) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    loadArchivesFromDir(file, archives);
                } else if (file.getName().endsWith(".gz")) {
                    archives.add(file.getName());
                }
            }
        }
    }
}
