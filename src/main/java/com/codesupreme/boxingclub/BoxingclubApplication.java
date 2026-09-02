package com.codesupreme.boxingclub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BoxingclubApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoxingclubApplication.class, args);
	}

}
