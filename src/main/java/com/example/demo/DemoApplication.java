package com.example.demo;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import java.util.TimeZone;

@SpringBootApplication
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