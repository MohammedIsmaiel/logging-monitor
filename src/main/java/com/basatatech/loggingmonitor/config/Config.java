package com.basatatech.loggingmonitor.config;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.file.tail.ApacheCommonsFileTailingMessageProducer;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.extern.log4j.Log4j2;

@Configuration
@Log4j2
public class Config {
    private static final String INBOUND_PATH = "/connects/logs";

    @Autowired
    private ApplicationContext appCtx;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Bean(name = "producer")
    public ApacheCommonsFileTailingMessageProducer apacheFileTailingMessageProducer() {
        ApacheCommonsFileTailingMessageProducer producer = new ApacheCommonsFileTailingMessageProducer();
        producer.setAutoStartup(false);
        producer.setOutputChannel(fileInputChannel());
        producer.setFile(new File(INBOUND_PATH + "test.txt"));
        producer.setPollingDelay(2000);
        return producer;
    }

    @Bean
    public MessageChannel fileInputChannel() {

        DirectChannel directChannel = new DirectChannel();
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
