package com.eg.tgcurrencybot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class TgcurrencybotApplication {

	public static void main(String[] args) {
		SpringApplication.run(TgcurrencybotApplication.class, args);
	}

}
