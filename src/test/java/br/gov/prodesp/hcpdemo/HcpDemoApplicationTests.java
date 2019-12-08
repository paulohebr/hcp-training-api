package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.hcpModel.HCPDirectory;
import br.gov.prodesp.hcpdemo.hcpModel.HCPEntry;
import br.gov.prodesp.hcpdemo.hcpModel.HCPObject;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.HCPFacet;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.HCPQueryObject;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.HCPQueryRequest;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.HCPSort;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.expression.HCPQueryExpressionBuilder;
import br.gov.prodesp.hcpdemo.hcpModel.query.response.HCPStatus;
import br.gov.prodesp.hcpdemo.model.MyObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;

@SpringBootTest(classes = HcpDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class HcpDemoApplicationTests {

    private MainService mainService;

    @Autowired
    public void setMainService(final MainService mainService) {
        this.mainService = mainService;
    }

    private ObjectMapper objectMapper;

    @Autowired
    public void setObjectMapper(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private WebTestClient webClient;

    @Autowired
    public void setWebTestClient(final WebTestClient webClient) {
        this.webClient = webClient;
    }

    @Test
    void contextLoads() {
    }

    @Test
    void objectWithAnnotation() {
        Mono<HCPObject> dataWithAnnotation = mainService.getDataWithAnnotation("InstallerSmallBanner.bmp");
        StepVerifier.create(dataWithAnnotation).expectSubscription().consumeNextWith(r -> {
            Object annotation = r.getAnnotation();
            Assertions.assertEquals(MyObject.class, annotation.getClass());
            MyObject myObject = (MyObject) annotation;
            Assertions.assertEquals("Paulo", myObject.getOwner());
            Assertions.assertEquals(9800, r.getData().length);
        }).expectComplete().verify();
    }

    byte[] getImage(MyObject myObject) {
        Font font = new Font("Arial", Font.PLAIN, 48);
        BufferedImage image = DemoHelper.textToImage(myObject.getOwner(), font, 100);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return outputStream.toByteArray();
    }

    MyObject getMyObject() {
        String user = "Paulo";
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        MyObject myObject = new MyObject();
        myObject.setId(1L);
        myObject.setOwner(user);
        myObject.setSignDate(zonedDateTime);
        return myObject;
    }

    Mono<FilePart> getFilePart(byte[] bytes) {
        Flux<DataBuffer> dataBufferFlux = Flux.just(new DefaultDataBufferFactory().wrap(bytes));
        FilePart fileMock = mock(FilePart.class);
        given(fileMock.content()).willReturn(dataBufferFlux);
        return Mono.just(fileMock);
    }

    @Test
    void testImageFilePartMono() {
        MyObject myObject = getMyObject();
        String filename = myObject.getOwner() + "-sign.png";
        File file = new File("C:\\tmp2\\" + filename);
        byte[] bytes = getImage(myObject);
        Mono<FilePart> image = getFilePart(bytes);
        Flux<DataBuffer> dataBufferFlux = image.flatMapMany(Part::content);
        StepVerifier.create(dataBufferFlux).consumeNextWith(r -> {
            Assertions.assertNotNull(r);
            try {
                byte[] writeBytes = ByteStreams.toByteArray(r.asInputStream());
                //noinspection UnstableApiUsage
                Files.write(writeBytes, file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).expectComplete().verify();
    }

    @Test
    void uploadToController() {
        MyObject myObject = getMyObject();
        uploadToController(myObject);
    }

    private void uploadToController(MyObject myObject) {
        String filename = myObject.getOwner() + "-sign.png";
        byte[] bytes = getImage(myObject);
        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("file", bytes).filename(filename).contentType(MediaType.IMAGE_PNG);
        try {
            bodyBuilder.part("annotation", objectMapper.writeValueAsString(myObject), MediaType.TEXT_PLAIN);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        bodyBuilder.part("size", String.valueOf(bytes.length), MediaType.TEXT_PLAIN);
        webClient
                .post()
                .uri("/upload-annotation/demo/" + filename)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void ingestDemo() {
        ZonedDateTime[] zonedDateTimes = new ZonedDateTime[10];
        ZonedDateTime now = ZonedDateTime.now();
        for (int i = 0; i < 10; i++) {
            zonedDateTimes[i] = now.minusDays(i + 1);
        }
        for (int i = 0; i < DemoHelper.NAMES.length; i++) {
            Long id = (long) (i + 1);
            String name = DemoHelper.NAMES[i];
            int leastSigDigit = i % 10;
            ZonedDateTime signDate = zonedDateTimes[leastSigDigit];
            MyObject myObject = new MyObject();
            myObject.setId(id);
            myObject.setOwner(name);
            myObject.setSignDate(signDate);
            uploadToController(myObject);
        }
    }

    @Test
    void directoryWithMetadata() {
        webClient.get()
                .uri("/directory/demo/")
                .exchange()
                .expectBody(HCPDirectory.class)
                .consumeWith(r -> {
                    HCPDirectory hcpDirectory = r.getResponseBody();
                    Assertions.assertNotNull(hcpDirectory);
                    Assertions.assertNotNull(hcpDirectory.getEntries());
                });
    }

    @Test
    void deleteAll() {
        webClient
                .get()
                .uri("/directory/")
                .exchange()
                .expectBody(HCPDirectory.class)
                .consumeWith(r -> {
                    HCPDirectory hcpDirectory = r.getResponseBody();
                    Assertions.assertNotNull(hcpDirectory);
                    Assertions.assertNotNull(hcpDirectory.getEntries());
                    for (HCPEntry entry : hcpDirectory.getEntries()) {
                        if (entry.getType().equals("object")) {
                            webClient
                                    .delete()
                                    .uri("/delete/" + entry.getUrlName())
                                    .exchange()
                                    .expectStatus().isOk();
                        }
                    }
                });
    }

    @Test
    void query() throws JsonProcessingException {
        List<String> properties = new ArrayList<>();
        properties.add("utf8Name");
        properties.add("hash");
        List<HCPFacet> facets = new ArrayList<>();
        facets.add(HCPFacet.namespace());
        List<HCPSort> sorts = new ArrayList<>();
        sorts.add(HCPSort.Asc("myobjectOwner"));
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        String query = HCPQueryExpressionBuilder.builder()
//                .add("myobjectId").range().must().start(1).end(50)
                .add("myobjectSigndate").range().must()
//                .add("ingestTimeString").range().must()
                .start(zonedDateTime.minusDays(4))
                .end(zonedDateTime.plusDays(1))
//                .add("myobjectOwner").many().must("Arthur").not("Adell").done()
//                .add("myobjectOwner").single().optional("Adell")
//                .add("myobjectId").many().optional(1).optional(2).done()
                .build();
        HCPQueryRequest hcpQueryRequest = HCPQueryObject.builder()
                .query(query)
                .contentProperties(true)
                .verbose(true)
                .facets(facets)
                .sort(sorts)
                .objectProperties(properties)
                .count(20)
                .build().toRequest();
        Mono<Tuple2<HCPStatus, List<HCPObject>>> mono = mainService
                .query(hcpQueryRequest)
                .flatMap(r -> {
                    Assertions.assertNotNull(r);
                    Assertions.assertNotNull(r.getResultSet());
                    Assertions.assertNotEquals(r.getResultSet().size(), 0);
                    Mono<List<HCPObject>> hcpObjectListMono = Flux
                            .fromIterable(r.getResultSet())
                            .flatMap(r2 -> {
                                Assertions.assertNotNull(r2);
                                Assertions.assertFalse(StringUtils.isEmpty(r2.getUrlName()));
                                return mainService.getObjectMetadataWithAnnotationFQDN(r2.getUrlName());
                            })
                            .collectList();
                    return Mono.zip(Mono.just(r.getStatus()), hcpObjectListMono);
                });
        StepVerifier.create(mono).consumeNextWith(r -> {
            Assertions.assertNotNull(r);
            Assertions.assertNotNull(r.getT1());
            Assertions.assertNotNull(r.getT2());
            Assertions.assertNotEquals(r.getT2().size(), 0);
        }).expectComplete().verify();
    }

}
