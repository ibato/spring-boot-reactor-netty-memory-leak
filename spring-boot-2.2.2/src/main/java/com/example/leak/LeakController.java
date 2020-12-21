package com.example.leak;

import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@RestController
public class LeakController {
	private final WebClient webClient;

	public LeakController(WebClient webClient) {
		this.webClient = webClient;
	}

	@GetMapping("/ok")
	public Mono<String> ok() {
		return Mono.just("OK");
	}

	@GetMapping("/leak")
	public Mono<ResponseEntity<Void>> leak(String uri) {
		return webClient.get()
			.uri(uri)
			.retrieve()
			.toBodilessEntity();
	}
}
