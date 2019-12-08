package br.gov.prodesp.hcpdemo.hcpModel.query.request.operation;

import br.gov.prodesp.hcpdemo.hcpModel.query.request.HCPQueryRequest;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class HCPQueryOperation {
    private Integer count;
    private HCPLastResult lastResult;
    private Boolean verbose;
    private List<String> objectProperties;
    private HCPSystemMetadata systemMetadata;

    public HCPQueryRequest toRequest() {
        return new HCPQueryRequest(null, this);
    }
}
