package com.basatatech.loggingmonitor.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.basatatech.loggingmonitor.util.FileUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogManager {
    private static final Map<String, LogManager> USER_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> LOG_PATH_MAP = new ConcurrentHashMap<>();
    private static final Map<String, String> ARCHIVE_PATH_MAP = new ConcurrentHashMap<>();
    private static final long SESSION_TIMEOUT = 10L * 60 * 1000; // 10 minutes in milliseconds
    private static final ScheduledExecutorService sessionCleanupExecutor = Executors.newScheduledThreadPool(1);

    static {
        // Schedule session cleanup task to run periodically
        sessionCleanupExecutor.scheduleAtFixedRate(LogManager::cleanupExpiredSessions, SESSION_TIMEOUT, SESSION_TIMEOUT,
                TimeUnit.MILLISECONDS);
    }

    private String filePath;
    private String topic;
    private SimpMessagingTemplate simpMessagingTemplate;
    private boolean watching = false;
    private WatchService watchService;
    private Thread watchThread;
    private long lastReadPosition = 0;
    private long lastActivityTime = 0;

    public void init(String logPath, SimpMessagingTemplate simpMessagingTemplate, String userId, boolean archive) {
        updateSessionActivity(userId); // Update session activity when initializing
        logSessionOpened(userId); // Log session opening
        this.simpMessagingTemplate = simpMessagingTemplate;
        this.filePath = logPath;
        this.topic = String.format("/topic/messages/%s/%s", userId, new File(logPath).getName());
        if (!archive) {
            this.watching = true;
        }
        log.info("TOPIC: {}", this.topic);
    }

    public String readFile(boolean archive) {
        log.info("read content for file: {}", filePath);
        if (archive) {
            return readEntireFile();
        } else {
            String initialContent = readInitialContent();
            simpMessagingTemplate.convertAndSend(FileUtil.removeLogSuffix(topic), initialContent);
            startFileWatcher();
            return initialContent;
        }
    }

    private String readEntireFile() {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            return builder.toString();
        } catch (IOException e) {
            log.error("Error reading the file: {}", e.getMessage());
            return "Error reading the file";
        }
    }

    private String readInitialContent() {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }
            lastReadPosition = new File(filePath).length();
        } catch (IOException e) {
            log.error("Error reading initial content of the file: {}", e.getMessage());
            return "Error reading the file";
        }
        return builder.toString();
    }

    private void startFileWatcher() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            Path path = Paths.get(filePath).getParent();
            path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            watchThread = new Thread(() -> {
                try {
                    WatchKey key;
                    while (watching && (key = watchService.take()) != null) {
                        for (WatchEvent<?> event : key.pollEvents()) {
                            Path changed = (Path) event.context();
                            if (changed.toString().equals(Paths.get(filePath).getFileName().toString())) {
                                readNewContent();
                            }
                        }
                        key.reset();
                    }
                } catch (InterruptedException e) {
                    log.warn("File watching interrupted");
                    Thread.currentThread().interrupt(); // Reset the interrupted status
                }
            });
            watchThread.start();
        } catch (IOException e) {
            log.error("Error starting file watcher: ", e);
        }
    }

    private void readNewContent() {
        try (SeekableByteChannel channel = Files.newByteChannel(Paths.get(filePath))) {
            channel.position(lastReadPosition); // Move to the last read position
            StringBuilder builder = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = channel.read(ByteBuffer.wrap(buffer))) != -1) {
                builder.append(new String(buffer, 0, bytesRead));
            }
            if (builder.length() > 0) {
                String message = builder.toString();
                simpMessagingTemplate.convertAndSend(FileUtil.removeLogSuffix(topic), message);
            }
            lastReadPosition = channel.position(); // Update the last read position
        } catch (IOException e) {
            log.error("Error reading new content of the file: ", e);
        }
    }

    public static void updateActiveUserSession(String userId) {
        log.info("Heartbeat received for user " + userId);
        LogManager logManager = USER_MAP.get(userId);
        if (logManager != null) {
            logManager.updateSessionActivity(userId);
        }
    }

    // Method to update session activity timestamp
    private void updateSessionActivity(String userId) {
        log.info("Update Session Activity for user " + userId);
        this.lastActivityTime = System.currentTimeMillis();
    }

    // Method to check and cleanup expired sessions
    private static void cleanupExpiredSessions() {
        log.info("I'm cleaning the expired session right now after " + (SESSION_TIMEOUT / 60000)
                + " minutes of inactive! ");
        long currentTime = System.currentTimeMillis();
        USER_MAP.entrySet().removeIf(entry -> {
            if ((currentTime - entry.getValue().lastActivityTime) > SESSION_TIMEOUT) {
                entry.getValue().stopWatching();
                return true;
            }
            return false;
        });
    }

    public static LogManager mapUserSession(String userId) {
        // Update session activity when accessing session
        LogManager logManager = USER_MAP.computeIfAbsent(userId, k -> new LogManager());
        logManager.updateSessionActivity(userId);
        return logManager;
    }

    public static void removeUserSession(String userId) {
        // Log session closing
        USER_MAP.computeIfPresent(userId, (key, value) -> {
            value.logSessionClosed(key);
            value.stopWatching(); // Stop watching for this session
            return null;
        });
        log.info("remove session for {}", userId);
        USER_MAP.remove(userId);
        logOpenSessions(); // Log remaining open sessions
        logOpenSessionUserIds(); // Log user IDs of remaining open sessions
    }

    public void stopWatching() {
        this.watching = false;
        if (watchThread != null) {
            watchThread.interrupt();
        }
        try {
            if (watchService != null) {
                watchService.close();
            }
        } catch (IOException e) {
            log.error("Error stopping file watcher: ", e);
        }
        log.info("-------------- Stop Watching -----------------------");
    }

    private void logSessionOpened(String userId) {
        log.info("Session opened for user: {}", userId);
    }

    private void logSessionClosed(String userId) {
        log.info("Session closed for user: {}", userId);
    }

    private static void logOpenSessions() {
        log.info("Total open sessions: {}", USER_MAP.size());
    }

    private static void logOpenSessionUserIds() {
        log.info("Open session user IDs: {}", USER_MAP.keySet());
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

    public static List<String> loadLogsNames(String logsDir) {
        return FileUtil.loadLogsNames(logsDir);
    }

    public static List<String> loadArchiveNames(String logName) {
        return FileUtil.loadArchiveNames(LOG_PATH_MAP.get(logName));
    }

}