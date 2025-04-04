package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

@EnableR2dbcRepositories
@SpringBootApplication
public class R2dbcDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(R2dbcDemoApplication.class, args);
	}

}
