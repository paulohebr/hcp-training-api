package br.gov.prodesp.hcpdemo;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = HcpDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class HCPDeleteTests {
    private WebTestClient webClient;
    @Autowired
    public void setWebTestClient(final WebTestClient webClient){
        this.webClient = webClient;
    }

    @Test
    @Order(5)
    void deleteAllObjects() {
        TestHelper.directoryContent("/", TestHelper.deleteObjectsConsumer(webClient), webClient);
    }


    @Test
    @Order(6)
    void deleteAllDirectories() {
        TestHelper.directoryContent("/", TestHelper.deleteDirectoriesConsumer(webClient), webClient);
    }
}
