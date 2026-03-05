package com.example.demo;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

@SpringBootApplication
@EnableAsync
public class DemoApplication  {

	@PostConstruct
	public void init() {
		// Fuerza a que toda la JVM use la hora de Colombia independientemente de donde esté el servidor de AWS
		TimeZone.setDefault(TimeZone.getTimeZone("America/Bogota"));
	}
	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}