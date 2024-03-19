package com.basatatech.loggingmonitor.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.integration.file.tail.ApacheCommonsFileTailingMessageProducer;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.AllArgsConstructor;

@Controller
@AllArgsConstructor
public class TestController {
    private static final Logger log = LogManager.getLogger(TestController.class);
    private static final String INBOUND_PATH = "/connects/logs/test.txt";

    private ApplicationContext applicationContext;

    @GetMapping("/m")
    public String readLog(Model model, @RequestParam String path) {
        log.info("--------------------{}-------------------", path);
        model.addAttribute("logEntries", readLogEntries(INBOUND_PATH));
        var producer = applicationContext.getBean(ApacheCommonsFileTailingMessageProducer.class);
        if (producer.isActive())
            producer.stop();
        producer.setFile(new File(path));
        producer.start();
        return "index";
    }

    private String readLogEntries(String path) {
        StringBuilder logsStringBuilder = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                logsStringBuilder.append("//" + line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return logsStringBuilder.toString();
    }
}
