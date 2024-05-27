package com.basatatech.loggingmonitor.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import com.basatatech.loggingmonitor.service.LoggerService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@Scope(value = WebApplicationContext.SCOPE_SESSION)
public class LoggingController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/logs/{userId}/{name}")
    public String readLog(@PathVariable String userId, @PathVariable String name, @RequestParam boolean archive) {
        log.info("--------------------{}-------------------", name);
        LoggerService loggerService = LoggerService.mapUserSession(userId);
        String logPath = archive ? LoggerService.getArchivePaths(name) : LoggerService.getLogPath(name);
        log.info("log path   {}", logPath);
        if (logPath != null) {
            loggerService.init(logPath, simpMessagingTemplate, userId);
            loggerService.tailFile();
            log.info("Return  readLog() " + name + "-----------------------------");
            return "done";
        } else {
            return "Log path not found";
        }
    }

    @GetMapping("/logs/{userId}/{name}/stop")
    public String stopLog(@PathVariable String userId, @PathVariable String name) {
        log.info("--------------------{}-------------------", name);
        LoggerService loggerService = LoggerService.mapUserSession(userId);
        loggerService.stopWatching();
        LoggerService.removeUserSession(userId);
        log.info("Return  stopLog()" + name + "-----------------------------");
        return "done";
    }

}
