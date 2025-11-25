package com.alcohol.alcoholdetectionsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class AlcoholDetectionSystemApplication {

	static {
		try {
			Dotenv dotenv = Dotenv.configure()
					.directory("./srcCode/BackEnd")
					.ignoreIfMissing()
					.load();
			dotenv.entries().forEach(entry ->
					System.setProperty(entry.getKey(), entry.getValue()));
		} catch (Exception e) {
			System.out.println(".env file not found");
		}
	}
	public static void main(String[] args) {
		SpringApplication.run(AlcoholDetectionSystemApplication.class, args);
	}

}
