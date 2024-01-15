package com.basatatech.loggingmonitor;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.file.tail.ApacheCommonsFileTailingMessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.context.WebApplicationContext;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class config {
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;
    private static final String INBOUND_PATH = "/connects/logs";

    // @Bean(name = "producer")
    // public ApacheCommonsFileTailingMessageProducer
    // apacheFileTailingMessageProducer() {
    // ApacheCommonsFileTailingMessageProducer producer = new
    // ApacheCommonsFileTailingMessageProducer();
    // producer.setOutputChannel(fileInputChannel());
    // producer.setFile(new File(INBOUND_PATH, "test.txt"));
    // producer.setPollingDelay(2000);
    // return producer;
    // }

    @Bean
    public MessageChannel fileInputChannel() {
        return new DirectChannel();
    }

    @Bean
    @ServiceActivator(inputChannel = "fileInputChannel")
    public MessageHandler fileInputHandler() {
        return message -> {
            // Your business logic to process the file content goes here
            log.info("Received file content: " + message.getPayload());
            simpMessagingTemplate.convertAndSend("/topic/messages", message);
        };
    }

}
