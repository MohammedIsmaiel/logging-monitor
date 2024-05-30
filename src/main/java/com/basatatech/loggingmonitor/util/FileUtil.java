package com.basatatech.loggingmonitor.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.basatatech.loggingmonitor.service.LogManager;

public class FileUtil {
    private FileUtil() {
        // Default constructor for FileUtil
    }

    public static List<String> loadLogsNames(String logsDir) {
        File path = new File(logsDir);
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
                    LogManager.storeLogPath(logName, file.getAbsolutePath());
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

    public static List<String> loadArchiveNames(String logDirPath) {
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
                        LogManager.storeArchivePaths(logName, file.getAbsolutePath());
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