package com.example.leak;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class LeakApplication {
	static {
		System.setProperty("io.netty.leakDetection.level", "PARANOID");
	}

	public static void main(String[] args) {
		SpringApplication.run(LeakApplication.class, args);
	}

}
