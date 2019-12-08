package br.gov.prodesp.hcpdemo.hcpModel.query.response;

import lombok.Data;

@Data
public class HCPFacetResult {
    private String property;
    private HCPFacetFrequency frequency;
}
