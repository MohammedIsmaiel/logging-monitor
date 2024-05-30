package com.basatatech.loggingmonitor.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Data
@Service
public class LogService {
    @Value("${logs.directory.path}")
    private String logsDir;
    private LogService logServiceInstance;

    @PostConstruct
    private void init() {
        logServiceInstance = this;
    }

    public static String readLog(String userId, String name, boolean archive,
            SimpMessagingTemplate simpMessagingTemplate) {
        LogManager logManager = LogManager.mapUserSession(userId);
        String logPath = archive ? LogManager.getArchivePath(name) : LogManager.getLogPath(name);
        if (logPath != null) {
            log.info("--------------------{}-------------------", name);
            logManager.init(logPath, simpMessagingTemplate, userId, archive);
            return logManager.readFile(archive);
        } else {
            return "Log path not found";
        }
    }

    public static String stopLog(String userId) {
        LogManager logManager = LogManager.mapUserSession(userId);
        if (logManager != null) {
            logManager.stopWatching();
            // loggerService.saveLastReadPosition(userId);
            LogManager.removeUserSession(userId);
            return "done";
        } else {
            return "Session not found";
        }
    }

    public List<String> loadLogsNames() {
        if (logServiceInstance == null) {
            throw new IllegalStateException("LogService is not initialized yet.");
        }
        return LogManager.loadLogsNames(logServiceInstance.getLogsDir());
    }

    public static List<String> loadArchiveNames(String logName) {
        return LogManager.loadArchiveNames(logName);
    }
}