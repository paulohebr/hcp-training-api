package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.hcpModel.HCPObject;
import br.gov.prodesp.hcpdemo.hcpModel.query.response.HCPQueryResult;
import br.gov.prodesp.hcpdemo.model.MyObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@SpringBootTest(classes = HcpDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HcpDemoApplicationTests {

	private MainService mainService;
	@Autowired
	public void setMainService(final MainService mainService){
	    this.mainService = mainService;
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

	@Test
	void query() throws JsonProcessingException {
		Mono<HCPQueryResult> mono = mainService.testQuery();
		StepVerifier.create(mono).consumeNextWith(r -> {
			Assertions.assertTrue(true);
		}).expectComplete().verify();
	}

}
