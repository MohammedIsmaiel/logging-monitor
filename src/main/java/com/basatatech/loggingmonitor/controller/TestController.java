package com.basatatech.loggingmonitor.controller;

import org.springframework.web.bind.annotation.RestController;

import com.basatatech.loggingmonitor.ProducerBean;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.file.tail.ApacheCommonsFileTailingMessageProducer;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@Slf4j
public class TestController {

    @Autowired
    private ApplicationContext appCtx;

    // @Autowired
    // private ApacheCommonsFileTailingMessageProducer producer;

    private static final String INBOUND_PATH = "/connects/logs";

    @GetMapping("/m/{connectName}")
    public String readLog(@PathVariable String connectName) {
        ((AnnotationConfigApplicationContext) appCtx).registerBean("apacheCommonsFileTailingMessageProducer",
                ApacheCommonsFileTailingMessageProducer.class,
                new ProducerBean().create(new File(INBOUND_PATH, connectName + ".txt")));
        return "index";
    }

    // @GetMapping("/m/:{connectName}")
    // public String changeFile(@PathVariable String connectName) {
    // producer.setFile(new File(INBOUND_PATH, connectName + ".txt"));
    // return "index";
    // }
}
