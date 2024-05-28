package com.basatatech.loggingmonitor.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;

import com.basatatech.loggingmonitor.service.LogService;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
@Scope(value = WebApplicationContext.SCOPE_SESSION)
public class LoggingController {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @GetMapping("/logs/{userId}/{name}")
    public String readLog(@PathVariable String userId, @PathVariable String name, @RequestParam boolean archive) {
        return LogService.readLog(userId, name, archive, simpMessagingTemplate);
    }

    @GetMapping("/logs/{userId}/{name}/stop")
    public String stopLog(@PathVariable String userId, @PathVariable String name) {
        return LogService.stopLog(userId, name);
    }
}
