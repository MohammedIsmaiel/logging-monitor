package com.basatatech.loggingmonitor.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.basatatech.loggingmonitor.exception.NoAvailableLogsException;
import com.basatatech.loggingmonitor.service.LoggerService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class RenderPage {
    @GetMapping({ "/", "/logs" })
    public String readLog(Model model) {
        log.info("-- ---------- RenderPage ----------------- --");
        List<String> logNames = LoggerService.loadLogsNames();
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
        LoggerService.removeUserSession(userId);
        return "redirect:/logout";
    }

    @GetMapping("/logs/{logName}/archives")
    public List<String> getLogArchives(@PathVariable String logName) {
        return LoggerService.loadArchiveNames(logName);
    }
}
