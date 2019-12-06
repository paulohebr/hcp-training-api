package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.hcpModel.HCPDirectory;
import br.gov.prodesp.hcpdemo.hcpModel.HCPObject;
import br.gov.prodesp.hcpdemo.model.MyObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.io.ByteStreams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyExtractors;
import org.springframework.web.reactive.function.BodyInserter;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Service
public class MainService {
    private final HcpWebClientService hcpWebClientService;
    private final XmlMapper xmlMapper;
    private final ObjectMapper objectMapper;
    private final DefaultDataBufferFactory dataBufferFactory;


    public MainService(HcpWebClientService hcpWebClientService, ObjectMapper objectMapper) {
        this.hcpWebClientService = hcpWebClientService;
        this.objectMapper = objectMapper;
        dataBufferFactory = new DefaultDataBufferFactory();
        xmlMapper = new XmlMapper();
        xmlMapper.findAndRegisterModules();
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Mono<ResponseEntity<byte[]>> getData(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.GET, path)
                .exchange()
                .flatMap(response -> response.toEntity(byte[].class));
    }

    public Mono<HCPObject> getDataWithAnnotation(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.GET, wholeObjectQueryParams(MyObject.ANNOTATION), path)
                .exchange()
                .flatMap(r -> Mono.zip(Mono.just(r.headers()), Mono.just(r.body(BodyExtractors.toDataBuffers()))))
                .flatMap(r -> {
                    HCPObject.HCPObjectBuilder hcpObjectBuilder = HCPObject.fromHeaders(r.getT1(), String.join("/", path));
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

    public Mono<ResponseEntity<Void>> createDirectory(String... path) {
        LinkedMultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("type", "directory");
        return hcpWebClientService.getAuthorizedWebClientForHCPRest( HttpMethod.PUT, queryParams, path).exchange().flatMap(ClientResponse::toBodilessEntity);
    }

    public Mono<HCPDirectory> getDirectory(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.GET, path)
                .exchange()
                .flatMap((response -> response.toEntity(String.class).map(HttpEntity::getBody)))
                .map(str -> {
                    try {
                        HCPDirectory hcpDirectory = xmlMapper.readValue(str, HCPDirectory.class);
                        if (hcpDirectory.getEntries() == null){
                            hcpDirectory.setEntries(new ArrayList<>());
                        }
                        return hcpDirectory;
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public Mono<HCPObject> getObject(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.GET, path)
                .exchange()
                .map(response -> {
                    ClientResponse.Headers headers = response.headers();
                    byte[] data = response.toEntity(byte[].class).map(HttpEntity::getBody).block();
                    return HCPObject.fromHeaders(headers, String.join("/", path))
                            .data(data)
                            .build();
                });
    }

    public Mono<HCPObject> getObjectMetadata(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.GET, path)
                .exchange()
                .map(response -> HCPObject.fromHeaders(response.headers(), String.join("/", path)).build());
    }

    public Mono<HCPObject> getObjectMetadataWithAnnotation(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.GET, customMetadataQueryParams(MyObject.ANNOTATION), path)
                .exchange()
                .flatMap(r -> Mono.zip(Mono.just(r.headers()), r.body(BodyExtractors.toMono(byte[].class))))
                .map(r -> {
                    try {
                        HCPObject.HCPObjectBuilder hcpObjectBuilder = HCPObject.fromHeaders(r.getT1(), String.join("/", path));
                        MyObject myObject = xmlMapper.readValue(r.getT2(), MyObject.class);
                        hcpObjectBuilder.annotation(myObject);
                        return hcpObjectBuilder.build();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public Mono<ClientResponse> postData(InputStream data, String... path) throws IOException {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.PUT, path)
//                .header("Content-Length", String.valueOf(data.available()))
                .body(BodyInserters.fromResource(new InputStreamResource(data)))
                .exchange();
    }

    public Mono<ResponseEntity<Void>> delete(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.DELETE, path)
                .exchange().flatMap(ClientResponse::toBodilessEntity);
    }

    public Mono<ResponseEntity<Void>> postData(Mono<FilePart> file, String... path) {
        return file.flatMap(f -> {
            Flux<DataBuffer> dataBuffer = f.content();
            BodyInserter<Flux<DataBuffer>, ReactiveHttpOutputMessage> bodyInsert = BodyInserters.fromDataBuffers(dataBuffer);
            return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.PUT, path).body(bodyInsert).exchange().flatMap(ClientResponse::toBodilessEntity);
        });
    }

    public Mono<ResponseEntity<Void>> postDataWithAnnotation(Mono<FilePart> filePart, Mono<FormFieldPart> annotationPart, Mono<FormFieldPart> sizePart, String... path) {
        return Mono.zip(filePart, annotationPart, sizePart).flatMap(r -> {
            try {
                Flux<DataBuffer> content = r.getT1().content();
                String json = r.getT2().value();
                MyObject myObject = objectMapper.readValue(json, MyObject.class);
                byte[] bytes = xmlMapper.writeValueAsBytes(myObject);
                Flux<DataBuffer> annotationBytes = Flux.just(dataBufferFactory.wrap(bytes));
                Flux<DataBuffer> data = content.concatWith(annotationBytes);
                BodyInserter<Flux<DataBuffer>, ReactiveHttpOutputMessage> bodyInsert = BodyInserters.fromDataBuffers(data);
                Long size = Long.valueOf(r.getT3().value());
                WebClient.RequestBodySpec authorizedWebClientForHCPRest = hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.PUT, wholeObjectQueryParams(MyObject.ANNOTATION), path);
                authorizedWebClientForHCPRest.header("X-HCP-Size", String.valueOf(size));
                return authorizedWebClientForHCPRest.body(bodyInsert).exchange().flatMap(ClientResponse::toBodilessEntity);
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
