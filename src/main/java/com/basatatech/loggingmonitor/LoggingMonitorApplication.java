package com.basatatech.loggingmonitor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class LoggingMonitorApplication {

	private static final String INBOUND_PATH = "/connects/logs";

	public static void main(String[] args) {

		SpringApplication.run(LoggingMonitorApplication.class, args);
	}

}
