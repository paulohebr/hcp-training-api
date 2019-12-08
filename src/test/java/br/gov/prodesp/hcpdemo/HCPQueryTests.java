package br.gov.prodesp.hcpdemo;

import br.gov.prodesp.hcpdemo.hcpModel.HCPObject;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.HCPFacet;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.HCPQueryObject;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.HCPQueryRequest;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.HCPSort;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.expression.HCPQueryExpressionBuilder;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.operation.HCPQueryOperation;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.operation.HCPSystemMetadata;
import br.gov.prodesp.hcpdemo.hcpModel.query.request.operation.HCPTransaction;
import br.gov.prodesp.hcpdemo.hcpModel.query.response.HCPQueryResult;
import br.gov.prodesp.hcpdemo.hcpModel.query.response.HCPStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest(classes = HcpDemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class HCPQueryTests {

    private MainService mainService;
    @Autowired
    public void setMainService(final MainService mainService){
        this.mainService = mainService;
    }

    @Test
    void query() {
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
                .count(10)
//                .offset(10L)
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
                                return mainService.getObjectMetadataWithAnnotationWithoutDataFQDN(r2.getUrlName());
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


    @Test
    void queryOperation() {
        HCPQueryRequest hcpQueryRequest = HCPQueryOperation.builder()
                .count(0)
                .systemMetadata(
                        HCPSystemMetadata.builder()
                                .replicationCollision(true)
                                .transactions(HCPTransaction.builder().transaction("create").build().asList())
                                .build())
                .build().toRequest();
        Mono<HCPQueryResult> mono = mainService.query(hcpQueryRequest);
        StepVerifier.create(mono).consumeNextWith(r -> {
            Assertions.assertNotNull(r);
            Assertions.assertNotNull(r.getStatus());
            Assertions.assertEquals(0L, r.getStatus().getResults());
        }).expectComplete().verify();
    }
}
