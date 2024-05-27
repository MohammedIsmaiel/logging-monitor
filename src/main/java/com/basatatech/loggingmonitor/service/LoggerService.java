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

import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggerService {
    private static final String LOGS_DIR = "/home/srvadmin/APP/logs/";
    private static final Map<String, LoggerService> USER_MAP = new HashMap<>();
    private static final Map<String, String> LOG_PATH_MAP = new HashMap<>();
    private static final Map<String, String> ARCHIVE_PATH_MAP = new HashMap<>();

    private String filePath;
    private String topic = "/topic/messages";
    private SimpMessagingTemplate simpMessagingTemplate;
    private boolean watching = false;

    public LoggerService() {
        // Default constructor for LoggerService
    }

    public void init(String logPath, SimpMessagingTemplate simpMessagingTemplate, String userId) {
        log.info("LoggerService init()");
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.filePath = logPath;
        this.topic = String.format("%s/%s/%s", topic, userId, new File(logPath).getName());
        this.watching = true;
        log.info("TOPIC: {}", this.topic);
    }

    public void tailFile() {
        log.info("LoggerService tailFile() for file: {}", filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder builder = new StringBuilder();
            while (watching) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info("Received file content: {}", line);
                    builder.append(line).append("\n");
                }
                if (builder.length() > 0) {
                    String message = builder.toString();
                    simpMessagingTemplate.convertAndSend(removeLogSuffix(topic), message);
                    log.info("Buffer Sent");
                    builder.setLength(0); // Clear the builder for the next iteration
                }
            }
            log.info("Exit While *************************");
        } catch (IOException e) {
            log.error("Error reading the file: {}", e.getMessage());
        }
    }

    public void stopWatching() {
        this.watching = false;
        log.info("-------------- Stop Watching -----------------------");
    }

    public static LoggerService mapUserSession(String userId) {
        return USER_MAP.computeIfAbsent(userId, k -> new LoggerService());
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

    public static String getArchivePaths(String archiveName) {
        return ARCHIVE_PATH_MAP.get(archiveName);
    }

    public static List<String> loadLogsNames() {
        File path = new File(LOGS_DIR);
        if (!path.exists() || !path.isDirectory()) {
            return Collections.emptyList();
        }

        List<String> logs = new ArrayList<>();
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
                    continue;
                }
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

    private static class LogsFileFilterName implements FilenameFilter {
        @Override
        public boolean accept(File dir, String name) {
            File file = new File(dir, name);
            return file.isDirectory() || name.endsWith(".log");
        }
    }

    public static List<String> loadArchiveNames(String logName) {
        String logDirPath = LOG_PATH_MAP.get(logName);
        if (logDirPath == null) {
            return Collections.emptyList();
        }

        List<String> archives = new ArrayList<>();
        File logDir = new File(logDirPath).getParentFile();
        if (logDir.exists() && logDir.isDirectory()) {
            loadArchivesFromDir(logDir, archives);
        }
        return archives;
    }

    private static void loadArchivesFromDir(File dir, List<String> archives) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    loadArchivesFromDir(file, archives);
                } else if (file.getName().endsWith(".txt")) {
                    String fileName = file.getName();
                    int lastDotIndex = fileName.lastIndexOf(".");
                    if (lastDotIndex != -1) {
                        String logName = fileName.substring(0, lastDotIndex);
                        archives.add(logName);
                        storeArchivePaths(logName, file.getAbsolutePath());
                    }
                }
            }
        }
    }

    public static String removeLogSuffix(String filename) {
        if (filename != null && (filename.endsWith(".log") || filename.endsWith(".txt"))) {
            return filename.substring(0, filename.length() - 4);
        }
        return filename;
    }
}