package com.basatatech.loggingmonitor.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

        model.addAttribute("logNames", logNames);

        return "logs";
    }

    private List<String> getLogNamesFromService() {
        return LoggerService.loadLogsNames();
    }
}
