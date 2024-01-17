package com.basatatech.loggingmonitor.util;

import java.io.File;
import java.io.FilenameFilter;

public class LogsFileFilterName implements FilenameFilter {

    @Override
    public boolean accept(File arg0, String name) {
        return name.endsWith(".log");
    }

}
