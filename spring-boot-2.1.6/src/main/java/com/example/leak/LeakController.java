package com.example.leak;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@RestController
public class LeakController {
	private static final Logger logger = LoggerFactory.getLogger(LeakApplication.class);
	private final WebClient webClient;

	public LeakController(WebClient webClient) {
		this.webClient = webClient;
	}

	@GetMapping("/leak")
	public Mono<String> leak(String uri) {
		return webClient.get()
			.uri(uri)
			.retrieve()
			.onStatus(Predicate.not(HttpStatus::is2xxSuccessful), res -> {
				logger.error("Fail to get uri: {}", uri);
				throw new RuntimeException("Fail to get uri: " + uri); // 여기서 exception 을 던지면
			})
			.bodyToMono(String.class); // 여기서 body 를 제대로 consume 하지 못함
	}
}
