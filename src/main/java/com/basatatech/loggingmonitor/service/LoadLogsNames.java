package com.basatatech.loggingmonitor.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.basatatech.loggingmonitor.util.LogsFileFilterName;

public class LoadLogsNames {

    private static final String dir = "/home/abdelhafeezahmed/logs/";

    public static List<String> loadLogsNames() {
        List<String> logs = new ArrayList<>();
        File path = new File(dir);
        if (!path.exists() || !path.isDirectory())
            return null;
        path.listFiles();
        File[] logFiles = path.listFiles(new LogsFileFilterName());
        for (File file : logFiles) {
            logs.add(file.getName());
        }
        return logs;
    }
}
