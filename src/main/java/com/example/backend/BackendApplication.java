package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {

		 Dotenv dotenv = Dotenv.configure()
                .ignoreIfMissing()   // không crash nếu thiếu .env
                .load();

        dotenv.entries().forEach(e ->
            System.setProperty(e.getKey(), e.getValue())
        );

		SpringApplication.run(BackendApplication.class, args);
	}

}
