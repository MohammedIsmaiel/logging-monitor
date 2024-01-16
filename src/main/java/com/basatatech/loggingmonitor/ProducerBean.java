package com.basatatech.loggingmonitor;

import java.io.File;

import org.springframework.integration.file.tail.ApacheCommonsFileTailingMessageProducer;

import lombok.AllArgsConstructor;

// @Component
@AllArgsConstructor
public class ProducerBean {

    // private MessageChannel fileInputChannel;

    public ApacheCommonsFileTailingMessageProducer create(File file) {
        ApacheCommonsFileTailingMessageProducer producer = new ApacheCommonsFileTailingMessageProducer();
        // producer.setOutputChannel(fileInputChannel);
        producer.setFile(file);
        producer.setPollingDelay(2000);
        return producer;
    }
}
