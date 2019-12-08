package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.hcpModel.*;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.HCPQueryRequest;
import br.gov.prodesp.hcpdemo.hcpModel.query.response.HCPQueryResult;
import br.gov.prodesp.hcpdemo.model.MyObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
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
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;

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
        xmlMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        SimpleModule module = new SimpleModule();
        module.addSerializer(new HCPZonedDateTimeJackson.Serializer());
        module.addDeserializer(ZonedDateTime.class, new HCPZonedDateTimeJackson.Deserializer());
        xmlMapper.registerModule(module);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public Mono<ResponseEntity<byte[]>> getData(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.GET, path)
                .exchange()
                .flatMap(response -> response.toEntity(byte[].class));
    }

    public Mono<HCPObject> getObjectDataAndMetadata(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.GET, path)
                .exchange()
                .map(response -> {
                    ClientResponse.Headers headers = response.headers();
                    byte[] data = response.toEntity(byte[].class).map(HttpEntity::getBody).block();
                    return HCPObject.fromHeaders(headers, path)
                            .data(data)
                            .build();
                });
    }

    public Mono<HCPObject> getObjectMetadataWithoutData(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.GET, path)
                .exchange()
                .map(response -> HCPObject.fromHeaders(response.headers(), path).build());
    }

    public Mono<HCPObject> getObjectDataAndMetadataAndAnnotation(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.GET, wholeObjectQueryParams(MyObject.ANNOTATION), path)
                .exchange()
                .flatMap(r -> Mono.zip(Mono.just(r.headers()), Mono.just(r.body(BodyExtractors.toDataBuffers()))))
                .flatMap(r -> {
                    HCPObject.HCPObjectBuilder hcpObjectBuilder = HCPObject.fromHeaders(r.getT1(), path);
                    Boolean customMetadataFirst = HCPHeader.fromHeaders(HCPHeader.HCP_CUSTOM_METADATA_FIRST, r.getT1()).asBoolean();
                    int firstPartSize = HCPHeader.fromHeaders(HCPHeader.HCP_SIZE, r.getT1()).asInteger();
                    if (customMetadataFirst) {
                        int contentLength = HCPHeader.fromHeaders(HCPHeader.CONTENT_LENGTH, r.getT1()).asInteger();
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
                        if (customMetadataFirst) {
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

    public Mono<HCPObject> getObjectMetadataWithAnnotationWithoutData(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.GET, customMetadataQueryParams(MyObject.ANNOTATION), path)
                .exchange()
                .flatMap(r -> Mono.zip(Mono.just(r.headers()), r.body(BodyExtractors.toMono(byte[].class))))
                .map(r -> {
                    try {
                        HCPObject.HCPObjectBuilder hcpObjectBuilder = HCPObject.fromHeaders(r.getT1(), path);
                        MyObject myObject = xmlMapper.readValue(r.getT2(), MyObject.class);
                        hcpObjectBuilder.annotation(myObject);
                        return hcpObjectBuilder.build();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public Mono<HCPObject> getObjectMetadataWithAnnotationWithoutDataFQDN(String fqdn) {
        String[] path = hcpWebClientService.extractPath(fqdn);
        return getObjectMetadataWithAnnotationWithoutData(path);
    }

    public Mono<ResponseEntity<Void>> createDirectory(String... path) {
        LinkedMultiValueMap<String, String> queryParams = HCPQueryParams.TYPE_DIRECTORY.asMultiValueMap();
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.PUT, queryParams, path).exchange().flatMap(ClientResponse::toBodilessEntity);
    }

    public Mono<HCPDirectory> getDirectory(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.GET, path)
                .exchange()
                .flatMap((response -> response.toEntity(String.class).map(HttpEntity::getBody)))
                .map(str -> {
                    try {
                        HCPDirectory hcpDirectory = xmlMapper.readValue(str, HCPDirectory.class);
                        if (hcpDirectory.getEntries() == null) {
                            hcpDirectory.setEntries(new ArrayList<>());
                        }
                        return hcpDirectory;
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public Mono<ClientResponse> postData(InputStream data, String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.PUT, path)
//                .header("Content-Length", String.valueOf(data.available()))
                .body(BodyInserters.fromResource(new InputStreamResource(data)))
                .exchange();
    }

    public Mono<ResponseEntity<Void>> postData(Mono<FilePart> file, String... path) {
        return file.flatMap(f -> {
            Flux<DataBuffer> dataBuffer = f.content();
            BodyInserter<Flux<DataBuffer>, ReactiveHttpOutputMessage> bodyInsert = BodyInserters.fromDataBuffers(dataBuffer);
            return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.PUT, path).body(bodyInsert).exchange().flatMap(ClientResponse::toBodilessEntity);
        });
    }

    public Mono<ResponseEntity<Void>> postDataWithAnnotation(Mono<FilePart> filePartMono, Mono<FormFieldPart> annotationPartMono, Mono<FormFieldPart> sizePartMono, String... path) {
        return Mono.zip(filePartMono, annotationPartMono, sizePartMono).flatMap(r -> {
            try {
                FilePart filePart = r.getT1();
                FormFieldPart annotationPart = r.getT2();
                FormFieldPart sizePart = r.getT3();
                Flux<DataBuffer> content = filePart.content();
                String json = annotationPart.value();
                MyObject myObject = objectMapper.readValue(json, MyObject.class);
                String xml = xmlMapper.writeValueAsString(myObject);
                byte[] bytes = xmlMapper.writeValueAsBytes(myObject);
                Flux<DataBuffer> annotationBytes = Flux.just(dataBufferFactory.wrap(bytes));
                Flux<DataBuffer> data = content.concatWith(annotationBytes);
                BodyInserter<Flux<DataBuffer>, ReactiveHttpOutputMessage> bodyInsert = BodyInserters.fromDataBuffers(data);
                Long size = Long.valueOf(sizePart.value());
                WebClient.RequestBodySpec authorizedWebClientForHCPRest = hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.PUT, wholeObjectQueryParams(MyObject.ANNOTATION), path);
                authorizedWebClientForHCPRest.header(HCPHeader.HCP_SIZE.getHeaderValue(), String.valueOf(size));
                return authorizedWebClientForHCPRest.body(bodyInsert).exchange().flatMap(ClientResponse::toBodilessEntity);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public Mono<ResponseEntity<Void>> delete(String... path) {
        return hcpWebClientService.getAuthorizedWebClientForHCPRest(HttpMethod.DELETE, path)
                .exchange().flatMap(ClientResponse::toBodilessEntity);
    }

    private static LinkedMultiValueMap<String, String> wholeObjectQueryParams(String annotation) {
        LinkedMultiValueMap<String, String> queryParams = HCPQueryParams.TYPE_WHOLE_OBJECT.asMultiValueMap();
        HCPQueryParams.annotation(annotation).assignTo(queryParams);
        return queryParams;
    }

    private static LinkedMultiValueMap<String, String> customMetadataQueryParams(String annotation) {
        LinkedMultiValueMap<String, String> queryParams = HCPQueryParams.TYPE_CUSTOM_METADATA.asMultiValueMap();
        HCPQueryParams.annotation(annotation).assignTo(queryParams);
        return queryParams;
    }

    public Mono<HCPQueryResult> query(HCPQueryRequest hcpQueryRequest) throws JsonProcessingException {
        WebClient.RequestBodySpec client = hcpWebClientService.getAuthorizedWebClientForHCPQuery();
        String xml = xmlMapper.writeValueAsString(hcpQueryRequest);
        return client.bodyValue(xml).retrieve().bodyToMono(String.class).map(r -> {
            try {
                return xmlMapper.readValue(r, HCPQueryResult.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
