package br.gov.prodesp.hcpdemo.hcpModel.query.request.operation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HCPLastResult {
    private String urlName;
    private String changeTimeMilliseconds;
    private Long version;
}
