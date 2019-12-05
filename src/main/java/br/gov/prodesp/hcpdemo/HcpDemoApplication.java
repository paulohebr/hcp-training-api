package br.gov.prodesp.hcpdemo;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.util.Locale;

@SpringBootApplication
public class HcpDemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(HcpDemoApplication.class, args);
	}

	@Bean
	public WebClient createWebClient() throws SSLException {
		SslContext sslContext = SslContextBuilder
				.forClient()
				.trustManager(InsecureTrustManagerFactory.INSTANCE)
				.build();
		HttpClient client = HttpClient.create().secure(t -> t.sslContext(sslContext));
		return WebClient.builder().clientConnector(new ReactorClientHttpConnector(client))
				.filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
					if (clientResponse.statusCode().isError()) {
						return Mono.error(new ResponseStatusException(HttpStatus.valueOf(clientResponse.statusCode().value()), clientResponse.statusCode().getReasonPhrase()));
					}
					return Mono.just(clientResponse);
				}))
				.build();
	}
}

