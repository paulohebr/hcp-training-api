package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.config.MainConfig;
import br.gov.prodesp.hcpdemo.hcpModel.HCPDirectory;
import br.gov.prodesp.hcpdemo.hcpModel.HCPObject;
import br.gov.prodesp.hcpdemo.model.MyObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.io.ByteStreams;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.buffer.*;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
@Service
public class MainService {
    private final WebClient webClient;
    private final MainConfig mainConfig;
    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;
    private final DefaultDataBufferFactory dataBufferFactory;
    private static final String ANNOTATION = "my-object";

    public MainService(WebClient webClient, MainConfig mainConfig, ObjectMapper objectMapper) {
        this.webClient = webClient;
        this.mainConfig = mainConfig;
        this.objectMapper = objectMapper;
        dataBufferFactory = new DefaultDataBufferFactory();
        xmlMapper = new XmlMapper();
        xmlMapper.findAndRegisterModules();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Mono<ResponseEntity<byte[]>> getData(String urlName) {
        return getAuthorizedWebClient(urlName, HttpMethod.GET)
                .exchange()
                .flatMap(response -> response.toEntity(byte[].class));
    }

    public Mono<HCPObject> getDataWithAnnotation(String urlName) {
        return getAuthorizedWebClient(urlName, HttpMethod.GET, wholeObjectQueryParams(ANNOTATION), 0L)
                .exchange()
                .flatMap(r -> Mono.zip(Mono.just(r.headers()), Mono.just(r.body(BodyExtractors.toDataBuffers()))))
                .flatMap(r -> {
                    HCPObject.HCPObjectBuilder hcpObjectBuilder = HCPObject.fromHeaders(r.getT1(), urlName);
                    Boolean customMetadataFirst = Boolean.valueOf(r.getT1().header("X-HCP-CustomMetadataFirst").stream().findFirst().orElseThrow(() -> new RuntimeException("no X-HCP-CustomMetadataFirst header present")));
                    int firstPartSize = Integer.parseInt(Objects.requireNonNull(r.getT1().header("X-HCP-Size").stream().findFirst().orElseThrow(() -> new RuntimeException("no X-HCP-Size header present"))));
                    if (customMetadataFirst){
                        int contentLength = Integer.parseInt(Objects.requireNonNull(r.getT1().header("Content-Length").stream().findFirst().orElseThrow(() -> new RuntimeException("no Content-Length header present"))));
                        firstPartSize = contentLength - firstPartSize;
                    }
                    return Mono.zip(Mono.just(hcpObjectBuilder), DataBufferUtils.join(r.getT2()), Mono.just(firstPartSize), Mono.just(customMetadataFirst));
                })
                .map(r -> {
                    try {
                        HCPObject.HCPObjectBuilder hcpObjectBuilder = r.getT1();
                        DataBuffer dataBufferData = r.getT2();
                        int firstPartSize = r.getT3();
                        Boolean customMetadataFirst = r.getT4();
                        byte[] bytes = ByteStreams.toByteArray(dataBufferData.asInputStream(true));
                        byte[] dataBytes;
                        byte[] annotationBytes;
                        if (customMetadataFirst){
                            annotationBytes = Arrays.copyOfRange(bytes, 0, firstPartSize);
                            dataBytes = Arrays.copyOfRange(bytes, firstPartSize, bytes.length);
                        } else {
                            dataBytes = Arrays.copyOfRange(bytes, 0, firstPartSize);
                            annotationBytes = Arrays.copyOfRange(bytes, firstPartSize, bytes.length);
                        }
                        MyObject myObject = xmlMapper.readValue(annotationBytes, MyObject.class);
                        hcpObjectBuilder.annotation(myObject);
                        hcpObjectBuilder.data(dataBytes);
                        return hcpObjectBuilder.build();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                });
    }

    public Mono<HCPDirectory> getDirectory(String id) {
        if ("root".equals(id)) {
            id = " ";
        }
        return getAuthorizedWebClient(id, HttpMethod.GET)
                .exchange()
                .flatMap((response -> response.toEntity(String.class).map(HttpEntity::getBody)))
                .map(str -> {
                    try {
                        return xmlMapper.readValue(str, HCPDirectory.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private WebClient.RequestBodySpec getAuthorizedWebClient(String urlName, HttpMethod get) {
        return getAuthorizedWebClient(urlName, get, null, 0L);
    }

    private WebClient.RequestBodySpec getAuthorizedWebClient(String urlName, HttpMethod httpMethod, LinkedMultiValueMap<String, String> queryParams, Long dataSize) {
        WebClient.RequestBodySpec authorizedWebClient = webClient
                .method(httpMethod)
                .uri(initUri(queryParams, urlName))
                .header("Authorization", "HCP " + mainConfig.getAuth());
        if (dataSize > 0) {
            authorizedWebClient = authorizedWebClient.header("X-HCP-Size", String.valueOf(dataSize));
        }
        return authorizedWebClient;
    }

    private Function<UriBuilder, URI> initUri(LinkedMultiValueMap<String, String> queryParams, String... path) {
        String[] restPath = new String[path.length + 1];
        System.arraycopy(path, 0, restPath, 1, path.length);
        restPath[0] = "rest";
        return builder -> {
            UriBuilder uriBuilder = builder
                    .scheme(mainConfig.getSchema())
                    .host(mainConfig.getHost())
                    .pathSegment(restPath);
            if (queryParams != null) {
                uriBuilder.queryParams(queryParams);
            }
            return uriBuilder.build();
        };
    }

    public Mono<HCPObject> getObject(String urlName) {
        return getAuthorizedWebClient(urlName, HttpMethod.GET)
                .exchange()
                .map(response -> {
                    ClientResponse.Headers headers = response.headers();
                    byte[] data = response.toEntity(byte[].class).map(HttpEntity::getBody).block();
                    return HCPObject.fromHeaders(headers, urlName)
                            .data(data)
                            .build();
                });
    }

    public Mono<HCPObject> getObjectMetadata(String urlName) {
        return getAuthorizedWebClient(urlName, HttpMethod.GET)
                .exchange()
                .map(response -> HCPObject.fromHeaders(response.headers(), urlName).build());
    }

    public Mono<HCPObject> getObjectMetadataWithAnnotation(String urlName) {
        return getAuthorizedWebClient(urlName, HttpMethod.GET, customMetadataQueryParams(ANNOTATION), 0L)
                .exchange()
                .flatMap(r -> Mono.zip(Mono.just(r.headers()), r.body(BodyExtractors.toMono(byte[].class))))
                .map(r -> {
                    try {
                        HCPObject.HCPObjectBuilder hcpObjectBuilder = HCPObject.fromHeaders(r.getT1(), urlName);
                        MyObject myObject = xmlMapper.readValue(r.getT2(), MyObject.class);
                        hcpObjectBuilder.annotation(myObject);
                        return hcpObjectBuilder.build();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public Mono<ClientResponse> postData(InputStream data, String id) throws IOException {
        return getAuthorizedWebClient(id, HttpMethod.PUT)
//                .header("Content-Length", String.valueOf(data.available()))
                .body(BodyInserters.fromResource(new InputStreamResource(data)))
                .exchange();
    }

    public Mono<ResponseEntity<Void>> delete(String urlName) {
        return getAuthorizedWebClient(urlName, HttpMethod.DELETE)
                .exchange().flatMap(ClientResponse::toBodilessEntity);
    }

    public Mono<ResponseEntity<Void>> postData(Mono<FilePart> file) {
        return file.flatMap(f -> {
            Flux<DataBuffer> dataBuffer = f.content();
            BodyInserter<Flux<DataBuffer>, ReactiveHttpOutputMessage> bodyInsert = BodyInserters.fromDataBuffers(dataBuffer);
            return getAuthorizedWebClient(f.filename(), HttpMethod.PUT).body(bodyInsert).exchange().flatMap(ClientResponse::toBodilessEntity);
        });
    }

    public Mono<ResponseEntity<Void>> postDataWithAnnotation(Mono<FilePart> filePart, Mono<FormFieldPart> annotationPart, Mono<FormFieldPart> sizePart) {
        return Mono.zip(filePart, annotationPart, sizePart).flatMap(r -> {
            try {
                String filename = r.getT1().filename();
                Flux<DataBuffer> content = r.getT1().content();
                String json = r.getT2().value();
                MyObject myObject = objectMapper.readValue(json, MyObject.class);
                byte[] bytes = xmlMapper.writeValueAsBytes(myObject);
                Flux<DataBuffer> annotationBytes = Flux.just(dataBufferFactory.wrap(bytes));
                Flux<DataBuffer> data = content.concatWith(annotationBytes);
                BodyInserter<Flux<DataBuffer>, ReactiveHttpOutputMessage> bodyInsert = BodyInserters.fromDataBuffers(data);
                Long size = Long.valueOf(r.getT3().value());
                return getAuthorizedWebClient(filename, HttpMethod.PUT, wholeObjectQueryParams(ANNOTATION), size).body(bodyInsert).exchange().flatMap(ClientResponse::toBodilessEntity);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static LinkedMultiValueMap<String, String> wholeObjectQueryParams(String annotation) {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("type", "whole-object");
        queryParams.add("annotation", annotation);
        return queryParams;
    }

    private static LinkedMultiValueMap<String, String> customMetadataQueryParams(String annotation) {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("type", "custom-metadata");
        queryParams.add("annotation", annotation);
        return queryParams;
    }
}
