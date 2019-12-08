package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.hcpModel.HCPDirectory;
import br.gov.prodesp.hcpdemo.hcpModel.HCPEndpoint;
import br.gov.prodesp.hcpdemo.hcpModel.HCPEntry;
import br.gov.prodesp.hcpdemo.model.MyObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TestHelper {
    public static byte[] generateImage(MyObject myObject) {
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

    public static void uploadToController(MyObject myObject, ObjectMapper objectMapper, WebTestClient webClient) {
        String filename = getFilename(myObject);
        byte[] bytes = generateImage(myObject);
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

    public static String getFilename(MyObject myObject){
        return myObject.getOwner() + "-sign.png";
    }

    public static MyObject[] getSample() {
        ZonedDateTime[] zonedDateTimes = new ZonedDateTime[10];
        ZonedDateTime now = ZonedDateTime.now();
        for (int i = 0; i < 10; i++) {
            zonedDateTimes[i] = now.minusDays(i + 1);
        }
        MyObject[] myObjects = new MyObject[50];
        for (int i = 0; i < DemoHelper.NAMES.length; i++) {
            Long id = (long) (i + 1);
            String name = DemoHelper.NAMES[i];
            int leastSigDigit = i % 10;
            ZonedDateTime signDate = zonedDateTimes[leastSigDigit];
            MyObject myObject = new MyObject();
            myObject.setId(id);
            myObject.setOwner(name);
            myObject.setSignDate(signDate);
            myObjects[i] = myObject;
        }
        return myObjects;
    }

    public static Consumer<EntityExchangeResult<HCPDirectory>> deleteDirectoriesConsumer(WebTestClient webClient) {
        return r -> {
            Assertions.assertNotNull(r);
            HCPDirectory hcpDirectory = r.getResponseBody();
            Assertions.assertNotNull(hcpDirectory);
            Assertions.assertNotNull(hcpDirectory.getEntries());
            String directoryPath = hcpDirectory.getUtf8Path();
            Assertions.assertNotNull(directoryPath);
            String path = Arrays.stream(directoryPath.split("/")).filter(s -> !s.equals(HCPEndpoint.REST.getEndpoint())).collect(Collectors.joining("/")) + "/";
            for (HCPEntry entry : hcpDirectory.getEntries()) {
                String urlName = path + entry.getUtf8Name();
                if (entry.getType().equals("directory") && !urlName.equals("/.lost+found")){
                    delete(urlName, webClient);
                }
            }
        };
    }

    public static void directoryContent(String uri, Consumer<EntityExchangeResult<HCPDirectory>> consumer, WebTestClient webClient) {
        uri = "/directory" + uri;
        webClient
                .get()
                .uri(uri)
                .exchange()
                .expectBody(HCPDirectory.class)
                .consumeWith(consumer);
    }

    public static Consumer<EntityExchangeResult<HCPDirectory>> deleteObjectsConsumer(WebTestClient webClient) {
        return r -> {
            Assertions.assertNotNull(r);
            HCPDirectory hcpDirectory = r.getResponseBody();
            Assertions.assertNotNull(hcpDirectory);
            String directoryPath = hcpDirectory.getUtf8Path();
            Assertions.assertNotNull(directoryPath);
            String path = Arrays.stream(directoryPath.split("/")).filter(s -> !s.equals(HCPEndpoint.REST.getEndpoint())).collect(Collectors.joining("/")) + "/";
            Assertions.assertNotNull(hcpDirectory.getEntries());
            for (HCPEntry entry : hcpDirectory.getEntries()) {
                String urlName = path + entry.getUtf8Name();
                if (entry.getType().equals("directory") && !urlName.equals("/.lost+found")) {
                    directoryContent(urlName, deleteObjectsConsumer(webClient), webClient);
                }
                if (entry.getType().equals("object")) {
                    delete(urlName, webClient);
                }
            }
        };
    }

    public static void delete(String urlName, WebTestClient webClient) {
        webClient
                .delete()
                .uri("/delete" + urlName)
                .exchange()
                .expectStatus().isOk();
    }
}
