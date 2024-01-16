package com.basatatech.loggingmonitor.controller;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.basatatech.loggingmonitor.service.FileTailingService;
import com.basatatech.loggingmonitor.service.LoggerService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class LoggingController {

    private final String basedir = "/home/mohamedahmedi/logs/";
    private final SimpMessagingTemplate simpMessagingTemplate;

    /// endpoint to render blank page with button

    @GetMapping("/logs/{name}")
    public String readLog(@PathVariable String name) {
        log.info("--------------------{}-------------------", name);
        String path = new StringBuilder().append(basedir).append(name).append(".log").toString();
        new LoggerService(path, simpMessagingTemplate).tailFile();
        // new FileTailingService(path, simpMessagingTemplate).tailFile();
        return "done";
    }

}
