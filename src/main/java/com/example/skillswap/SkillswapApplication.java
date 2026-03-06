package com.example.skillswap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SkillswapApplication {

	public static void main(String[] args) {
		SpringApplication.run(SkillswapApplication.class, args);
	}

}
