package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.hcpModel.HCPDirectory;
import br.gov.prodesp.hcpdemo.model.MyObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = HcpDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class HCPIngestTest {
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
    @Order(1)
    void ingestDemo() {
        for (MyObject myObject : TestHelper.getSample()) {
            TestHelper.uploadToController(myObject, objectMapper ,webClient);
        }
    }

    @Test
    @Order(2)
    void getDirectory() {
        TestHelper.directoryContent("/demo", r -> {
            HCPDirectory hcpDirectory = r.getResponseBody();
            Assertions.assertNotNull(hcpDirectory);
            Assertions.assertNotNull(hcpDirectory.getEntries());
            Assertions.assertEquals(50, hcpDirectory.getEntries().size());
        }, webClient);
    }
}
