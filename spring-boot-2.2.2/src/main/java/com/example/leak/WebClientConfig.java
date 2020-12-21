package com.example.leak;

import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

@Configuration
public class WebClientConfig {
	@Bean
	public WebClient webClient(WebClient.Builder builder) {
		return builder
			.clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient()).keepAlive(false)))
			.build();
	}

	@Bean
	public TcpClient tcpClient() {
		int connectionTimeout = 1000;
		int readTimeout = 1000;
		int writeTimeout = 1000;

		return TcpClient.create()
			.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout)
			.doOnConnected(connection ->
				connection.addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
					.addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS)));
	}
}
