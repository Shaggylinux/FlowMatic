package com.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

import org.springframework.boot.persistence.autoconfigure.EntityScan;

@SpringBootApplication
@EnableAsync
@EnableCaching
@EntityScan(basePackages = {"com.back", "org.springframework.modulith.events.jpa"})
public class BackApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackApplication.class, args);
	}

}
