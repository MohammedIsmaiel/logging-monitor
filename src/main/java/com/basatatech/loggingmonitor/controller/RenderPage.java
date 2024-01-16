package com.basatatech.loggingmonitor.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller

public class RenderPage {
    @GetMapping("/logs")
    public String readLog() {
        return "logs";
    }
}
