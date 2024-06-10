package com.basatatech.loggingmonitor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.basatatech.loggingmonitor.service.LogManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtil {
    private FileUtil() {
        // Default constructor for FileUtil
    }

    // public static String readEntireFile(String filePath) {
    // try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
    // StringBuilder builder = new StringBuilder();
    // String line;
    // while ((line = reader.readLine()) != null) {
    // builder.append(line).append("\n");
    // }
    // return builder.toString();
    // } catch (IOException e) {
    // log.error("Error reading the file: {}", e.getMessage());
    // return "Error reading the file";
    // }
    // }

    // public static String readInitialContent(String filePath, LogManager
    // logManager) {
    // StringBuilder builder = new StringBuilder();
    // try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
    // String line;
    // while ((line = reader.readLine()) != null) {
    // builder.append(line).append("\n");
    // }
    // logManager.setLastReadPosition(new File(filePath).length());
    // } catch (IOException e) {
    // log.error("Error reading initial content of the file: {}", e.getMessage());
    // return "Error reading the file";
    // }
    // return builder.toString();
    // }

    // public static void startFileWatcher(String filePath, LogManager logManager) {
    // try {
    // WatchService watchService = FileSystems.getDefault().newWatchService();
    // Path path = Paths.get(filePath).getParent();
    // path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
    // Thread watchThread = new Thread(() -> {
    // try {
    // WatchKey key;
    // while (logManager.isWatching() && (key = watchService.take()) != null) {
    // for (WatchEvent<?> event : key.pollEvents()) {
    // Path changed = (Path) event.context();
    // if (changed.toString().equals(Paths.get(filePath).getFileName().toString()))
    // {
    // readNewContent(filePath, logManager);
    // }
    // }
    // key.reset();
    // }
    // } catch (InterruptedException e) {
    // log.warn("File watching interrupted");
    // Thread.currentThread().interrupt(); // Reset the interrupted status
    // }
    // });
    // watchThread.start();
    // logManager.setWatchService(watchService);
    // logManager.setWatchThread(watchThread);
    // } catch (IOException e) {
    // log.error("Error starting file watcher: ", e);
    // }
    // }

    // public static void readNewContent(String filePath, LogManager logManager) {
    // try (SeekableByteChannel channel = Files.newByteChannel(Paths.get(filePath)))
    // {
    // channel.position(logManager.getLastReadPosition()); // Move to the last read
    // position
    // StringBuilder builder = new StringBuilder();
    // byte[] buffer = new byte[1024];
    // int bytesRead;
    // while ((bytesRead = channel.read(ByteBuffer.wrap(buffer))) != -1) {
    // builder.append(new String(buffer, 0, bytesRead));
    // }
    // if (builder.length() > 0) {
    // String message = builder.toString();
    // logManager.getSimpMessagingTemplate().convertAndSend(removeLogSuffix(logManager.getTopic()),
    // message);
    // }
    // logManager.setLastReadPosition(channel.position()); // Update the last read
    // position
    // } catch (IOException e) {
    // log.error("Error reading new content of the file: ", e);
    // }
    // }

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