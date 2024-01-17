package com.basatatech.loggingmonitor.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.basatatech.loggingmonitor.service.LoggerService;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class RenderPage {
    @GetMapping("/logs")
    public String readLog(Model model) {
        log.info("-- ---------- RenderPage ----------------- --");
        // Assume you have a service method that returns a list of log names
        List<String> logNames = getLogNamesFromService();
        if (logNames == null || logNames.isEmpty()) {

        }
        model.addAttribute("logNames", logNames);
        return "logs";
    }

    @GetMapping("/logs/{userId}/logout")
    public String getMethodName(@PathVariable String userId) {
        LoggerService.removeUserSession(userId);
        return "redirect:/logout";
    }

    private List<String> getLogNamesFromService() {
        return LoggerService.loadLogsNames();
    }
}
