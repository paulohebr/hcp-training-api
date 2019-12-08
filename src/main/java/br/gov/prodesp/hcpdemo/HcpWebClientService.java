package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.config.HCPConfig;
import br.gov.prodesp.hcpdemo.hcpModel.HCPEndpoint;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.util.Arrays;
import java.util.function.Function;

@Service
public class HcpWebClientService {
    private final HCPConfig HCPConfig;
    private final WebClient webClient;

    public HcpWebClientService(HCPConfig HCPConfig) throws SSLException {
        this.HCPConfig = HCPConfig;
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
        return getAuthorizedWebClientForHCP(initUri(HCPEndpoint.REST, queryParams, path), httpMethod, HCPConfig.getNamespaceHostname());
    }

    public WebClient.RequestBodySpec getAuthorizedWebClientForHCPQuery() {
        return getAuthorizedWebClientForHCP(initUri(HCPEndpoint.QUERY, null, "/"), HttpMethod.POST, HCPConfig.getTenantHostname())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML_VALUE)
                .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE);
    }

    private WebClient.RequestBodySpec getAuthorizedWebClientForHCP(Function<UriBuilder, URI> uriFunction, HttpMethod httpMethod, String hostname) {
//        Function<UriBuilder, URI> uriFunction = initUri(HCPEndpoint.REST, queryParams, urlName);
        return webClient
                .method(httpMethod)
                .uri(uriFunction)
                .header("Authorization", "HCP " + HCPConfig.getAuth())
                .header("Host", hostname);
    }

    private Function<UriBuilder, URI> initUri(HCPEndpoint hcpEndpoint, LinkedMultiValueMap<String, String> queryParams, String... path) {
        if (path.length == 1 && path[0].equals("/")) {
            path = new String[]{""};
        }
        String[] parentPath = new String[path.length + 1];
        System.arraycopy(path, 0, parentPath, 1, path.length);
        parentPath[0] = hcpEndpoint.getEndpoint();
        return builder -> {
            UriBuilder uriBuilder = builder
                    .scheme(HCPConfig.getSchema())
                    .host(HCPConfig.getIp())
                    .pathSegment(parentPath);
            if (queryParams != null) {
                uriBuilder.queryParams(queryParams);
            }
            return uriBuilder.build();
        };
    }

    public String[] extractPath(String fqdn) {
        String prefix = HCPConfig.getSchema() + "://" + HCPConfig.getNamespaceHostname();
        int i = fqdn.indexOf(prefix);
        if (i < 0){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "invalid fqdn " + fqdn);
        }
        String substring = fqdn.replace(prefix, "");
        return Arrays.stream(substring.split("/")).filter(s -> !StringUtils.isEmpty(s)).filter(s -> !s.equals(HCPEndpoint.REST.getEndpoint())).toArray(String[]::new);
    }
}
