package br.gov.prodesp.hcpdemo.hcpModel.query.request.operation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HCPNamespace {
    private String namespace;
}
