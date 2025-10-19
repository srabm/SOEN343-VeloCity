package com.concordia.velocity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;

@SpringBootApplication(
		scanBasePackages = {"config", "controller", "service", "model"},
		exclude = {R2dbcAutoConfiguration.class}
)

public class BackendApplication {
	public static void main(String[] args) {
		System.out.println("🚀 Starting VeloCity Backend...");
		SpringApplication.run(BackendApplication.class, args);
		System.out.println("✅ VeloCity Backend started successfully!");
	}
}