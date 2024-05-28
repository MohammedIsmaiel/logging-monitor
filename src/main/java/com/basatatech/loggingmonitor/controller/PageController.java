package com.basatatech.loggingmonitor.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.ModelAndView;

import com.basatatech.loggingmonitor.exception.NoAvailableLogsException;
import com.basatatech.loggingmonitor.service.LogService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class PageController {
    @GetMapping({ "/", "/logs" })
    public String readLog(Model model) {
        log.info("Rendering logs page...");
        List<String> logNames = LogService.loadLogsNames();
        if (logNames == null || logNames.isEmpty()) {
            throw new NoAvailableLogsException("No Available Logs to show...");
        }
        model.addAttribute("logNames", logNames);
        return "logs";
    }

    @GetMapping("/logs/error")
    public ModelAndView showErrorPage(Model model) {
        ModelAndView modelAndView = new ModelAndView("error");
        modelAndView.addObject("errorMessage", "Sorry, the page you are looking for is not found.");
        return modelAndView;
    }

    @GetMapping("/logs/{userId}/logout")
    public String getMethodName(@PathVariable String userId) {
        LogService.removeUserSession(userId);
        return "redirect:/logout";
    }

    @GetMapping("/logs/{logName}/archives")
    public ResponseEntity<List<String>> getLogArchives(@PathVariable String logName) {
        try {
            List<String> archiveNames = LogService.loadArchiveNames(logName);
            return ResponseEntity.ok(archiveNames);
        } catch (Exception e) {
            log.error("Error loading archives for log: " + logName, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }
}