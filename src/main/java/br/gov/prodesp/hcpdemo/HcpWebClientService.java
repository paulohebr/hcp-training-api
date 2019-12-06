package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.config.MainConfig;
import br.gov.prodesp.hcpdemo.hcpModel.HCPEndpoint;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.util.function.Function;

@Service
public class HcpWebClientService {
    private final MainConfig mainConfig;
    private final WebClient webClient;

    public HcpWebClientService(MainConfig mainConfig) throws SSLException {
        this.mainConfig = mainConfig;
        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();
        HttpClient client = HttpClient.create().secure(t -> t.sslContext(sslContext));
        webClient = WebClient.builder().clientConnector(new ReactorClientHttpConnector(client))
                .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                    if (clientResponse.statusCode().isError()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.valueOf(clientResponse.statusCode().value()), "HCP Response: " + clientResponse.statusCode().getReasonPhrase()));
                    }
                    return Mono.just(clientResponse);
                }))
                .build();
    }

    public WebClient.RequestBodySpec getAuthorizedWebClientForHCPRest(HttpMethod get, String... path) {
        return getAuthorizedWebClientForHCPRest(get, null, path);
    }

    public WebClient.RequestBodySpec getAuthorizedWebClientForHCPRest(HttpMethod httpMethod, LinkedMultiValueMap<String, String> queryParams, String... path) {
        return getAuthorizedWebClientForHCP(initUri(HCPEndpoint.REST, queryParams, path), httpMethod);
    }

    private WebClient.RequestBodySpec getAuthorizedWebClientForHCP(Function<UriBuilder, URI> uriFunction, HttpMethod httpMethod) {
//        Function<UriBuilder, URI> uriFunction = initUri(HCPEndpoint.REST, queryParams, urlName);
        return webClient
                .method(httpMethod)
                .uri(uriFunction)
                .header("Authorization", "HCP " + mainConfig.getAuth())
                .header("Host", mainConfig.getNamespaceHost());
    }

    private Function<UriBuilder, URI> initUri(HCPEndpoint hcpEndpoint, LinkedMultiValueMap<String, String> queryParams, String... path) {
        if (path.length == 1 && path[0].equals("/")){
            path = new String[]{""};
        }
        String[] parentPath = new String[path.length + 1];
        System.arraycopy(path, 0, parentPath, 1, path.length);
        parentPath[0] = hcpEndpoint.getEndpoint();
        return builder -> {
            UriBuilder uriBuilder = builder
                    .scheme(mainConfig.getSchema())
                    .host(mainConfig.getIp())
                    .pathSegment(parentPath);
            if (queryParams != null) {
                uriBuilder.queryParams(queryParams);
            }
            return uriBuilder.build();
        };
    }
}
